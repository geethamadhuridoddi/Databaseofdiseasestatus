import urllib.request
import urllib.error
try:
    response = urllib.request.urlopen('http://127.0.0.1:8000/api/disease/1/')
    print(response.read().decode('utf-8'))
except urllib.error.HTTPError as e:
    print(e.read().decode('utf-8', errors='ignore'))
except Exception as e:
    print(f"Error: {e}")
