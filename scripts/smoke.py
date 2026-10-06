"""Exercise the local Compose stack with fictional data; Python standard library only."""
import json
import time
import uuid
import urllib.request
import urllib.error

BASE = 'http://localhost:8080'

def request(route, body=None, headers=None):
    payload = None if body is None else json.dumps(body).encode()
    req = urllib.request.Request(BASE + route, data=payload, headers={'Content-Type':'application/json', **(headers or {})})
    try:
        with urllib.request.urlopen(req, timeout=5) as response:
            raw=response.read()
            return response.status, json.loads(raw) if raw else {}
    except urllib.error.HTTPError as error:
        raw=error.read()
        return error.code, json.loads(raw) if raw else {}

for _ in range(90):
    try:
        if request('/actuator/health')[0] == 200: break
    except urllib.error.URLError: pass
    time.sleep(2)
else: raise RuntimeError('API did not become ready')

with urllib.request.urlopen('http://localhost:4200',timeout=10) as response:
    assert b'<app-root>' in response.read(), 'Angular shell missing'

key = str(uuid.uuid4())
body = {'amount': 10, 'currency': 'PEN', 'scenario': 'LOST_RESPONSE'}
headers = {'Idempotency-Key': key}
assert request('/api/payments', body, headers)[0] == 504
assert request('/api/payments/' + key)[1]['status'] == 'APPROVED'
assert request('/api/payments', body, headers)[0] == 200
assert request('/api/payments/' + key + '/reversals', {})[1]['status'] == 'REVERSED'

print('payment-simulator: HTTP smoke passed')
