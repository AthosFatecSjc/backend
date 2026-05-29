import json, urllib.request, urllib.error
url="http://localhost:8181/ops/emergency/emails/usuarios-nao-deletados"
data=json.dumps({"subject":"Comunicado importante","body":"Identificamos um incidente e estamos usando o canal oficial de comunicação."}).encode("utf-8")
req=urllib.request.Request(url, data=data, headers={"Content-Type":"application/json","X-Emergency-Mail-Key":"d1b6f46ec0c410d0b8ba6fd793c2cd995ee12edddb12f76c"}, method="POST")
try:
    resp=urllib.request.urlopen(req)
    print(resp.status)
    print(resp.read().decode("utf-8"))
except urllib.error.HTTPError as e:
    print("ERROR", e.code)
    print(e.read().decode("utf-8"))
