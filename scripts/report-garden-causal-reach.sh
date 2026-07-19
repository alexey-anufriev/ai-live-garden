#!/usr/bin/env bash
set -euo pipefail

state_file="${1:-data/garden-state.txt}"
if [[ ! -f "$state_file" ]]; then
  echo "Garden state not found: ${state_file}" >&2
  exit 1
fi

awk -F '|' '
  function clamp(value) { return value < 0 ? 0 : (value > 100 ? 100 : value) }
  function carrier(name) { return traitCounts[name] + 0 }
  /^nutrients=/ { split($0, pair, "="); nutrients = pair[2] + 0 }
  /^nutrientBuffer=/ { split($0, pair, "="); buffer = pair[2] + 0 }
  /^organism=/ {
    type = $2
    if (type == "MOSS" || type == "ROOT_NETWORK" || type == "SPORE" || type == "FERN" || type == "FUNGUS") plants++
    if (type == "BEETLE" || type == "HARE" || type == "FOX") animals++
    traits = $6
    gsub(/\\,/, ",", traits)
    valueCount = split(traits, values, ",")
    delete seen
    for (i = 1; i <= valueCount; i++) {
      trait = values[i]
      if (trait == "" || seen[trait]++) continue
      traitCounts[trait]++
      if (type == "MOSS" && trait == "nutrient-conserver") mossConservers++
      if (type == "MOSS" && trait == "moss-nutrient-scavenger") mossScavengers++
      if (type == "FERN" && trait == "nutrient-conserver") fernConservers++
    }
  }
  END {
    reductionFactor = nutrients < 10 ? 1 : 5
    plantReduction = int((mossConservers + mossScavengers + fernConservers) / reductionFactor)
    demandRegulators = carrier("nutrient-demand-regulator")
    rootReduction = int(demandRegulators / 2)
    baseDemand = int(plants / 5)
    effectiveDemand = baseDemand - plantReduction - rootReduction
    if (effectiveDemand < 0) effectiveDemand = 0
    production = 2 + int(animals / 2)
    nutrientDelta = production - effectiveDemand
    mobilizers = carrier("nutrient-mobilizer") + carrier("fungal-nutrient-mobilizer")
    releasers = carrier("buffer-releaser")
    accelerators = carrier("buffer-release-accelerator") + carrier("buffer-release-optimizer")
    releaseRate = nutrients < 5 ? 2 : (nutrients < 10 ? 5 : 10)
    releaseRate = releaseRate - mobilizers - releasers - accelerators
    if (releaseRate < 1) releaseRate = 1
    if (buffer > 80) {
      releaseRate = int(releaseRate / 2)
      if (releaseRate < 1) releaseRate = 1
    }
    released = int(buffer / releaseRate)
    siphoned = carrier("buffer-siphon") * 5
    if (siphoned > buffer) siphoned = buffer
    rawNext = nutrients + nutrientDelta + released + siphoned
    clampedNext = clamp(rawNext)
    clampRisk = rawNext < 0 ? "lower" : (rawNext > 100 ? "upper" : "none")

    print "## Current Causal Reach Diagnostics"
    print ""
    print "These values are computed from the committed living state before task selection. An omitted resource-flow trait has zero carriers; verify any other exact trait with the count command below. Do not tune a zero-carrier trait unless the same change supplies and tests a credible adoption path."
    print ""
    print "### Nutrient Phase Estimate"
    print ""
    printf "- plants: %d; animals: %d; current nutrients: %d; buffer: %d\n", plants, animals, nutrients, buffer
    printf "- production: %d; base plant demand: %d; active plant reduction: %d; active demand-regulator reduction: %d\n", production, baseDemand, plantReduction, rootReduction
    printf "- effective demand: %d; raw nutrient delta before buffer: %d\n", effectiveDemand, nutrientDelta
    printf "- buffer release: %d at rate %d; siphon contribution: %d\n", released, releaseRate, siphoned
    printf "- raw next nutrients: %d; clamped next nutrients: %d; clamp risk: %s\n", rawNext, clampedNext, clampRisk
    print "- This is a phase-level estimate. Root/fungal buffer contributions and later feeding/death phases can still change the final tick result."
    print ""
    print "### Active Resource-Trait Carriers"
    print ""
    resourceCount = 0
    for (trait in traitCounts) {
      if (traitCounts[trait] > 0 && trait ~ /(nutrient|buffer|metabolic|scavenger|recycler|releaser|mobilizer|conserver|regulator)/) {
        resourceTraits[++resourceCount] = trait
      }
    }
    for (i = 2; i <= resourceCount; i++) {
      value = resourceTraits[i]
      j = i - 1
      while (j >= 1 && resourceTraits[j] > value) {
        resourceTraits[j + 1] = resourceTraits[j]
        j--
      }
      resourceTraits[j + 1] = value
    }
    for (i = 1; i <= resourceCount; i++) {
      trait = resourceTraits[i]
      printf "- %s: %d\n", trait, traitCounts[trait]
    }
    if (resourceCount == 0) print "- none"
    print ""
    print "Use `scripts/count-garden-trait-carriers.sh TRAIT` to verify any exact trait considered by the task."
  }
' "$state_file"
