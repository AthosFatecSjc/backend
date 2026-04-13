import json
from typing import Any
from urllib import request
from urllib.error import HTTPError, URLError

from app.core.config import INTERNAL_LOG_API_KEY, INTERNAL_LOG_URL


class InternalLogClient:
    def __init__(self, url: str = INTERNAL_LOG_URL, api_key: str = INTERNAL_LOG_API_KEY):
        self._url = url
        self._api_key = api_key

    def emit(
        self,
        event: str,
        result: str,
        description: str,
        metadata: str,
        target_ref: str,
    ) -> None:
        if not self._url:
            raise RuntimeError("INTERNAL_LOG_URL nao configurada")
        if not self._api_key:
            raise RuntimeError("INTERNAL_LOG_API_KEY nao configurada")

        payload: dict[str, Any] = {
            "event": event,
            "result": result,
            "description": description,
            "metadata": metadata,
            "targetRef": target_ref,
        }

        req = request.Request(
            self._url,
            data=json.dumps(payload).encode("utf-8"),
            headers={
                "Content-Type": "application/json",
                "X-Internal-Log-Key": self._api_key,
            },
            method="POST",
        )

        try:
            with request.urlopen(req, timeout=10) as response:
                if response.status not in (200, 201, 202, 204):
                    raise RuntimeError(f"Falha ao registrar log interno: status={response.status}")
        except HTTPError as exc:
            raise RuntimeError(f"Falha ao registrar log interno: status={exc.code}") from exc
        except URLError as exc:
            raise RuntimeError("Falha de conexao ao registrar log interno") from exc
