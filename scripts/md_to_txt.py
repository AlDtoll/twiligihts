#!/usr/bin/env python3
"""
Convert .md file to .txt (same content, different extension).
Usage: python scripts/md_to_txt.py path/to/file.md [output_dir]
Output: /tmp/<filename>.txt (or output_dir/<filename>.txt)
"""
import sys
import os
import shutil

def main():
    if len(sys.argv) < 2:
        print("Usage: python scripts/md_to_txt.py <file.md> [output_dir]")
        sys.exit(1)

    src = sys.argv[1]
    if not os.path.exists(src):
        print(f"Error: file not found: {src}")
        sys.exit(1)

    output_dir = sys.argv[2] if len(sys.argv) > 2 else "/tmp"
    basename = os.path.splitext(os.path.basename(src))[0]
    dst = os.path.join(output_dir, f"{basename}.txt")

    shutil.copy2(src, dst)
    print(dst)

if __name__ == "__main__":
    main()
