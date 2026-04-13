import sys
import unittest
from unittest.mock import MagicMock, patch

from app.jobs.import_aneel_perdas import main


class ImportAneelPerdasJobTest(unittest.TestCase):
    @patch("app.jobs.import_aneel_perdas.run_perdas_import")
    @patch("app.jobs.import_aneel_perdas.InternalLogClient")
    def test_emite_start_e_success(self, log_client_cls: MagicMock, run_import: MagicMock) -> None:
        run_import.return_value = {"raw_rows": 10, "loaded_rows": 8}
        client = MagicMock()
        log_client_cls.return_value = client

        with patch.object(sys, "argv", ["import_aneel_perdas"]):
            main()

        self.assertEqual(client.emit.call_count, 2)

        start_call = client.emit.call_args_list[0].kwargs
        success_call = client.emit.call_args_list[1].kwargs

        self.assertEqual(start_call["event"], "ANEEL_EXTRACTION_START")
        self.assertEqual(start_call["result"], "SUCCESS")
        self.assertIn("routine=import_aneel_perdas", start_call["metadata"])

        self.assertEqual(success_call["event"], "ANEEL_EXTRACTION_SUCCESS")
        self.assertEqual(success_call["result"], "SUCCESS")
        self.assertIn("raw_rows=10", success_call["metadata"])
        self.assertIn("loaded_rows=8", success_call["metadata"])
        self.assertEqual(start_call["target_ref"], success_call["target_ref"])

    @patch("app.jobs.import_aneel_perdas.run_perdas_import")
    @patch("app.jobs.import_aneel_perdas.InternalLogClient")
    def test_emite_start_e_fail(self, log_client_cls: MagicMock, run_import: MagicMock) -> None:
        run_import.side_effect = ValueError("falha; detalhe\nquebra")
        client = MagicMock()
        log_client_cls.return_value = client

        with patch.object(sys, "argv", ["import_aneel_perdas"]):
            with self.assertRaises(ValueError):
                main()

        self.assertEqual(client.emit.call_count, 2)

        start_call = client.emit.call_args_list[0].kwargs
        fail_call = client.emit.call_args_list[1].kwargs

        self.assertEqual(start_call["event"], "ANEEL_EXTRACTION_START")
        self.assertEqual(fail_call["event"], "ANEEL_EXTRACTION_FAIL")
        self.assertEqual(fail_call["result"], "FAIL")
        self.assertIn("error=falha, detalhe quebra", fail_call["metadata"])
        self.assertEqual(start_call["target_ref"], fail_call["target_ref"])

    @patch("app.jobs.import_aneel_perdas.run_perdas_import")
    @patch("app.jobs.import_aneel_perdas.InternalLogClient")
    def test_nao_bloqueia_coleta_quando_log_start_falha(
        self,
        log_client_cls: MagicMock,
        run_import: MagicMock,
    ) -> None:
        run_import.return_value = {"raw_rows": 2, "loaded_rows": 2}
        client = MagicMock()
        client.emit.side_effect = [RuntimeError("indisponivel"), None]
        log_client_cls.return_value = client

        with patch.object(sys, "argv", ["import_aneel_perdas"]):
            main()

        run_import.assert_called_once()
        self.assertEqual(client.emit.call_count, 2)

    @patch("app.jobs.import_aneel_perdas.run_perdas_import")
    @patch("app.jobs.import_aneel_perdas.InternalLogClient")
    def test_nao_bloqueia_erro_original_quando_log_fail_falha(
        self,
        log_client_cls: MagicMock,
        run_import: MagicMock,
    ) -> None:
        run_import.side_effect = ValueError("erro da coleta")
        client = MagicMock()
        client.emit.side_effect = [None, RuntimeError("indisponivel")]
        log_client_cls.return_value = client

        with patch.object(sys, "argv", ["import_aneel_perdas"]):
            with self.assertRaises(ValueError):
                main()

        run_import.assert_called_once()
        self.assertEqual(client.emit.call_count, 2)


if __name__ == "__main__":
    unittest.main()
