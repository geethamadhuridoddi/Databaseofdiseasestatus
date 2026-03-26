import urllib.request
import urllib.error

try:
    req = urllib.request.Request("http://127.0.0.1:8000/api/disease/1/")
    response = urllib.request.urlopen(req)
    print("STATUS 200:", response.read().decode())
except urllib.error.HTTPError as e:
    print(f"STATUS {e.code}:")
    print(e.read().decode())
