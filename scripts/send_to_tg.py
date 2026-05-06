#!/usr/bin/env python3
"""
Send a file or text message to a Telegram chat.
Usage:
  python scripts/send_to_tg.py <chat_id> --file path/to/file.txt
  python scripts/send_to_tg.py <chat_id> --text "message text"
  python scripts/send_to_tg.py <chat_id> --md path/to/file.md   (converts and sends as .txt)

Bot token is read from ~/.claude/channels/telegram/.env
"""
import sys
import os
import argparse
import shutil
import urllib.request
import urllib.parse
import json

ENV_FILE = os.path.expanduser("~/.claude/channels/telegram/.env")

def load_token():
    with open(ENV_FILE) as f:
        for line in f:
            line = line.strip()
            if line.startswith("TELEGRAM_BOT_TOKEN="):
                return line.split("=", 1)[1]
    raise RuntimeError("TELEGRAM_BOT_TOKEN not found in " + ENV_FILE)

def send_message(token, chat_id, text):
    url = f"https://api.telegram.org/bot{token}/sendMessage"
    data = urllib.parse.urlencode({"chat_id": chat_id, "text": text}).encode()
    req = urllib.request.Request(url, data=data)
    with urllib.request.urlopen(req) as resp:
        result = json.loads(resp.read())
    if not result.get("ok"):
        raise RuntimeError(f"Telegram error: {result}")
    print(f"Message sent (id={result['result']['message_id']})")

def send_document(token, chat_id, filepath):
    import http.client, mimetypes
    boundary = "----FormBoundary7MA4YWxkTrZu0gW"
    filename = os.path.basename(filepath)
    mime_type = mimetypes.guess_type(filename)[0] or "text/plain"

    with open(filepath, "rb") as f:
        file_data = f.read()

    body = (
        f"--{boundary}\r\n"
        f'Content-Disposition: form-data; name="chat_id"\r\n\r\n'
        f"{chat_id}\r\n"
        f"--{boundary}\r\n"
        f'Content-Disposition: form-data; name="document"; filename="{filename}"\r\n'
        f"Content-Type: {mime_type}\r\n\r\n"
    ).encode() + file_data + f"\r\n--{boundary}--\r\n".encode()

    conn = http.client.HTTPSConnection("api.telegram.org")
    conn.request(
        "POST",
        f"/bot{token}/sendDocument",
        body,
        {"Content-Type": f"multipart/form-data; boundary={boundary}"},
    )
    resp = conn.getresponse()
    result = json.loads(resp.read())
    conn.close()

    if not result.get("ok"):
        raise RuntimeError(f"Telegram error: {result}")
    print(f"Document sent (id={result['result']['message_id']}): {filename}")

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("chat_id", help="Telegram chat_id")
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("--file", help="Path to file to send as document")
    group.add_argument("--text", help="Text message to send")
    group.add_argument("--md", help="Path to .md file (converted to .txt and sent)")
    args = parser.parse_args()

    token = load_token()

    if args.text:
        send_message(token, args.chat_id, args.text)
    elif args.file:
        send_document(token, args.chat_id, args.file)
    elif args.md:
        basename = os.path.splitext(os.path.basename(args.md))[0]
        tmp_path = f"/tmp/{basename}.txt"
        shutil.copy2(args.md, tmp_path)
        send_document(token, args.chat_id, tmp_path)

if __name__ == "__main__":
    main()
