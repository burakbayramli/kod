import re, sys
from pathlib import Path

def convert_math_delimiters(text: str) -> str:
    """
    Convert ChatGPT-style math delimiters:
      [ ... ]  →  $$ ... $$
      ( ... )  →  $ ... $
    and replace copy-button comma spaces (", ") with LaTeX thin space "\\,"
    inside math expressions.
    """
    def fix_commas_in_math(math_str: str) -> str:
        # Replace ", " with LaTeX thin space, but not inside numbers (e.g. 1,000)
        return re.sub(r",(?!\d)", r"\\,", math_str)

    result = []
    inside_block = False

    for line in text.splitlines():
        # Handle single-line block math
        if "[" in line and "]" in line:
            line = re.sub(r"\[(.*?)\]", lambda m: "$$" + fix_commas_in_math(m.group(1)) + "$$", line)
            result.append(line)
            continue

        if "[" in line:
            inside_block = True
            line = line.replace("[", "$$")
        if "]" in line:
            inside_block = False
            line = line.replace("]", "$$")
            line = re.sub(r",(?!\d)", r"\\,", line)
            result.append(line)
            continue

        if inside_block:
            # Inside block math, only replace comma-spaces
            line = re.sub(r",(?!\d)", r"\\,", line)
        else:
            # Outside block math, convert inline math ( ... )
            line = re.sub(r"\(([^()\n]+?)\)", lambda m: "$" + fix_commas_in_math(m.group(1)) + "$", line)

        result.append(line)

    return "\n".join(result)


if __name__ == "__main__":
    infile = Path(sys.argv[1])
    outfile = Path("GPT-mathjax_fixed.md")
    text = infile.read_text(encoding="utf-8")
    fixed = convert_math_delimiters(text)
    outfile.write_text(fixed, encoding="utf-8")

    print(f"✅ Converted and cleaned: {infile} → {outfile}")
