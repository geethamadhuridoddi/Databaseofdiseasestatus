import urllib.request
import urllib.error
import urllib.parse
import json

try:
    url = "http://127.0.0.1:8000/api/cases/active/?user_id=1"
    req = urllib.request.Request(url)
    response = urllib.request.urlopen(req)
    print("STATUS 200:")
    data = json.loads(response.read().decode())
    print(f"Returned {len(data)} records")
except urllib.error.HTTPError as e:
    print(f"STATUS {e.code}:")
    print(e.read().decode())
