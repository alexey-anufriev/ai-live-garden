#!/usr/bin/env bash
set -euo pipefail

garden_output_file="${1:-agent/garden-trends.svg}"
organism_output_file="${2:-agent/organism-trends.svg}"
sample_limit="${GARDEN_TREND_SAMPLE_LIMIT:-40}"
row_step=46

recent_state_commits() {
  git log --since='7 days ago' --reverse --format='%H' -- data/garden-state.txt 2>/dev/null
}

display_label() {
  local value="$1"
  tr '[:upper:]_' '[:lower:] ' <<<"$value" |
    awk '{
      for (i = 1; i <= NF; i++) {
        $i = toupper(substr($i, 1, 1)) substr($i, 2)
      }
      print
    }'
}

sample_series() {
  awk -v limit="$sample_limit" '
    NF {
      values[++count] = $1
    }
    END {
      if (count == 0) {
        print 0
        exit
      }
      if (count <= limit) {
        for (i = 1; i <= count; i++) print values[i]
        exit
      }
      for (i = 1; i <= limit; i++) {
        selected = int(((i - 1) * (count - 1)) / (limit - 1)) + 1
        print values[selected]
      }
    }
  '
}

write_series_file() {
  local key="$1"
  local path="$2"

  awk -F'\t' -v key="$key" -v snapshots="$snapshot_count" '
    $2 == key {
      values[$1] = $3
    }
    END {
      for (i = 1; i <= snapshots; i++) {
        print ((i in values) ? values[i] : 0)
      }
    }
  ' "$snapshots_file" | sample_series > "$path"
}

append_snapshot() {
  local id="$1"

  awk -F'[=|]' -v id="$id" '
    /^#/ || /^$/ {
      next
    }
    /^organism=/ {
      total++
      type[$3]++
      next
    }
    /^[^=]+=.*$/ {
      param[$1] = $2
      next
    }
    END {
      printf "%s\torganisms\t%d\n", id, total + 0
      for (key in param) {
        if (key == "nutrients" || key == "nutrientBuffer" || key == "light" || key == "moisture" || key == "warmth") {
          printf "%s\t%s\t%s\n", id, key, param[key]
        }
      }
      for (organism_type in type) {
        printf "%s\torganismType:%s\t%d\n", id, organism_type, type[organism_type]
      }
    }
  '
}

build_snapshot_cache() {
  : > "$snapshots_file"

  while IFS= read -r commit; do
    snapshot_count=$((snapshot_count + 1))
    git show "${commit}:data/garden-state.txt" 2>/dev/null | append_snapshot "$snapshot_count" >> "$snapshots_file"
  done < <(recent_state_commits)

  snapshot_count=$((snapshot_count + 1))
  append_snapshot "$snapshot_count" < data/garden-state.txt >> "$snapshots_file"
}

organism_types_seen() {
  awk -F'\t' '$2 ~ /^organismType:/ { sub(/^organismType:/, "", $2); print $2 }' "$snapshots_file" | sort -u
}

write_svg_row() {
  local path="$1"
  local label="$2"
  local color="$3"
  local y="$4"
  local separator="${5:-true}"

  awk -v label="$label" -v color="$color" -v y="$y" -v separator="$separator" '
    NF {
      values[++count] = $1 + 0
      raw[count] = $1
      if (count == 1 || values[count] < min) min = values[count]
      if (count == 1 || values[count] > max) max = values[count]
    }
    END {
      x0 = 180
      width = 410
      height = 34
      if (count == 0) {
        count = 1
        values[1] = 0
        raw[1] = "0"
        min = 0
        max = 0
      }
      for (i = 1; i <= count; i++) {
        x = x0
        if (count > 1) x = x0 + ((i - 1) * width / (count - 1))
        if (max == min) {
          point_y = y + (height / 2)
        } else {
          point_y = y + height - (((values[i] - min) / (max - min)) * height)
        }
        points = points sprintf("%.1f,%.1f ", x, point_y)
      }
      if (separator == "true") {
        printf "  <line x1=\"24\" y1=\"%.1f\" x2=\"714\" y2=\"%.1f\" class=\"separator\" />\n", y - 6, y - 6
      }
      printf "  <text x=\"24\" y=\"%.0f\" class=\"label\">%s</text>\n", y + 23, label
      printf "  <polyline points=\"%s\" fill=\"none\" stroke=\"%s\" stroke-width=\"3\" stroke-linecap=\"round\" stroke-linejoin=\"round\" />\n", points, color
      printf "  <text x=\"618\" y=\"%.0f\" class=\"axis-value\">%s</text>\n", y + 8, max
      printf "  <text x=\"618\" y=\"%.0f\" class=\"axis-value\">%s</text>\n", y + height, min
    }
  ' "$path"
}

write_svg_header() {
  local title="$1"
  local desc="$2"
  local height="$3"

  cat <<SVG
<svg xmlns="http://www.w3.org/2000/svg" width="760" height="${height}" viewBox="0 0 760 ${height}" role="img" aria-labelledby="title desc">
  <title id="title">${title}</title>
  <desc id="desc">${desc}</desc>
  <rect width="760" height="${height}" rx="8" fill="#ffffff"/>
  <style>
    text { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; }
    .title { font-size: 18px; font-weight: 700; fill: #1f2933; }
    .subtitle { font-size: 12px; fill: #52606d; }
    .label { font-size: 13px; font-weight: 650; fill: #1f2933; }
    .axis-value { font-size: 11px; fill: #52606d; }
    .separator { stroke: #eef2f7; stroke-width: 1; }
  </style>
  <text x="24" y="32" class="title">${title}</text>
  <text x="24" y="52" class="subtitle">Last 7 days range</text>
SVG
}

mkdir -p "$(dirname "$garden_output_file")" "$(dirname "$organism_output_file")"
tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT
snapshots_file="$tmp_dir/snapshots.tsv"
snapshot_count=0

build_snapshot_cache

write_series_file organisms "$tmp_dir/organisms.tsv"
write_series_file nutrients "$tmp_dir/nutrients.tsv"
write_series_file nutrientBuffer "$tmp_dir/nutrient-buffer.tsv"
write_series_file light "$tmp_dir/light.tsv"
write_series_file moisture "$tmp_dir/moisture.tsv"
write_series_file warmth "$tmp_dir/warmth.tsv"

mapfile -t organism_types < <(organism_types_seen)
for organism_type in "${organism_types[@]}"; do
  write_series_file "organismType:${organism_type}" "$tmp_dir/organism-${organism_type}.tsv"
done

garden_svg_height=$((70 + (5 * row_step)))
organism_row_count=$((1 + ${#organism_types[@]}))
organism_svg_height=$((70 + (organism_row_count * row_step)))
palette=("#805ad5" "#d53f8c" "#dd6b20" "#3182ce" "#b7791f" "#319795" "#e53e3e" "#4a5568" "#6b46c1" "#2b6cb0" "#975a16" "#285e61")

{
  write_svg_header "Garden Trends" "Seven-day trends for garden resources and conditions." "$garden_svg_height"
  write_svg_row "$tmp_dir/nutrients.tsv" "Nutrients" "#b7791f" 75 false
  write_svg_row "$tmp_dir/nutrient-buffer.tsv" "Nutrient buffer" "#3182ce" $((75 + row_step))
  write_svg_row "$tmp_dir/light.tsv" "Light" "#dd6b20" $((75 + (row_step * 2)))
  write_svg_row "$tmp_dir/moisture.tsv" "Moisture" "#319795" $((75 + (row_step * 3)))
  write_svg_row "$tmp_dir/warmth.tsv" "Warmth" "#c53030" $((75 + (row_step * 4)))
  echo '</svg>'
} > "$garden_output_file"

{
  write_svg_header "Organism Trends" "Seven-day trends for total organisms and organism types." "$organism_svg_height"
  write_svg_row "$tmp_dir/organisms.tsv" "Total organisms" "#2f855a" 75 false
  for index in "${!organism_types[@]}"; do
    organism_type="${organism_types[$index]}"
    color="${palette[$((index % ${#palette[@]}))]}"
    y=$((75 + ((index + 1) * row_step)))
    write_svg_row "$tmp_dir/organism-${organism_type}.tsv" "$(display_label "$organism_type")" "$color" "$y"
  done
  echo '</svg>'
} > "$organism_output_file"
