with open('README.md', 'r') as f:
    lines = f.readlines()

new_lines = []
in_block = False
for line in lines:
    if '<!-- AI-LIVE-GARDEN:STATE-START -->' in line:
        new_lines.append(line)
        new_lines.append('**Garden Health:** Stable — Cycle 2602 reached; nutrients absent but nutrient buffer at maximum capacity.\n')
        new_lines.append('The garden persists in a state of nutrient scarcity, relying on the nutrient buffer to sustain the plant population.\n')
        in_block = True
    elif '<!-- AI-LIVE-GARDEN:STATE-END -->' in line:
        new_lines.append(line)
        in_block = False
    elif not in_block:
        new_lines.append(line)

with open('README.md', 'w') as f:
    f.writelines(new_lines)
