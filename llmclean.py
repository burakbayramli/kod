import re
import sys
from pathlib import Path

def clean_file_content(file_path):
    path = Path(file_path)
    
    # 1. Verification
    if not path.is_file():
        print(f"Error: The path '{file_path}' is not a valid file.")
        return

    try:
        content = path.read_text(encoding='utf-8')

        patterns = [
            (r'\*\*', ''),
            (r'\[cite_start\]', ''),
            (r'\[cite:\s*\d+(?:,\s*\d+)*\]', ''),
            (r'^####\s*(.*)$', r'\1\n'),
            (r'^###\s*(.*)$', r'\1\n'),
            (r'^##\s*(.*)$', r'\1\n'),
            (r'^\d+\.\s*', '')
        ]

        # 3. Apply all replacements
        for pattern, replacement in patterns:
            # Using MULTILINE is essential for the ^ and $ anchors
            content = re.sub(pattern, replacement, content, flags=re.MULTILINE)
            
        # 4. In-place write
        path.write_text(content, encoding='utf-8')
        print(f"Success: Processed '{file_path}'")

    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python script_name.py <filename>")
    else:
        target_file = sys.argv[1]
        clean_file_content(target_file)
