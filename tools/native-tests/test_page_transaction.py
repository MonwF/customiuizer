"""Execute production C++ page transactions using Linux syscalls (native Linux or WSL)."""
import os
import subprocess
import unittest
import tempfile
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


@unittest.skipUnless(os.name == 'nt' or sys.platform.startswith('linux'), 'Linux or WSL required')
class PageTransactionTests(unittest.TestCase):
    @staticmethod
    def linux_path(path):
        path = path.resolve()
        return '/mnt/' + path.drive[0].lower() + path.as_posix()[2:] if os.name == 'nt' else str(path)

    @classmethod
    def setUpClass(cls):
        root = cls.linux_path(ROOT)
        cls.prefix = ['wsl', '-d', 'Ubuntu', '--'] if os.name == 'nt' else []
        if os.name == 'nt':
            cls.binary = subprocess.check_output(cls.prefix + ['mktemp', '/tmp/hc-transaction-XXXXXX'], text=True).strip()
        else:
            fd, cls.binary = tempfile.mkstemp(prefix='folder-transaction-')
            os.close(fd)
        subprocess.run(cls.prefix + ['c++', '-std=c++20',
                        '-pthread', '-Wl,--wrap=mprotect', '-I' + root + '/tools/native-tests/stubs',
                        root + '/tools/native-tests/test_page_transaction.cpp', '-o', cls.binary],
                       check=True, text=True, encoding='utf-8')

    @classmethod
    def tearDownClass(cls):
        subprocess.run(cls.prefix + ['rm', '-f', cls.binary], check=True)

    def run_case(self, case):
        result = subprocess.run(self.prefix + [self.binary, case],
                                capture_output=True, text=True, encoding='utf-8')
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_dynamic_parameters(self):
        for rows in range(5, 9):
            with self.subTest(rows=rows):
                self.replay('live-replay-dynamic-' + str(rows))

    def test_marker_only(self):
        self.replay('live-replay-marker')

    def test_layout_only(self):
        self.replay('live-replay-layout')

    def test_marker_failure(self):
        self.replay('live-replay-marker-failure')

    def test_configuration_no_write(self):
        self.run_case('config')

    def test_density_scan_does_not_read_beyond_text(self):
        self.run_case('density-text-boundary')

    def test_relocates_positive_and_negative_adrp_veneer(self):
        self.run_case('adrp-veneer')

    def test_chains_existing_cell_height_hook(self):
        self.replay('live-replay-veneer')

    def test_preserves_existing_foreign_hook(self):
        self.run_case('existing')

    def test_preserves_later_foreign_hook(self):
        self.run_case('later')

    def test_accepts_a_different_existing_child_row_value(self):
        self.run_case('child-value')

    def test_merges_changes_between_capture_and_install(self):
        self.run_case('between')

    def test_transaction_reverses_two_changes_on_one_page(self):
        self.run_case('transaction')

    def test_transaction_reverses_first_change_when_second_syscall_fails(self):
        self.run_case('transaction-failure')

    def replay(self, mode):
        evidence = Path(os.environ.get('FOLDER_NATIVE_EVIDENCE', str(ROOT / 'test-evidence')))
        if not (evidence / 'height-live-page.bin').exists():
            self.skipTest('recorded live pages required')
        path = self.linux_path(evidence)
        result = subprocess.run(self.prefix + [self.binary,
                                 mode, path], capture_output=True,
                                text=True, encoding='utf-8')
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_drag_window_accepts_existing_row_override(self):
        self.replay('live-replay-drag-overlap')

    def test_full_install_on_recorded_live_pages(self):
        self.replay('live-replay')

    def test_full_install_adopts_already_satisfied_child_hook(self):
        self.replay('live-replay-satisfied')

    def test_full_install_reverses_all_density_pages_after_postcheck_failure(self):
        self.replay('live-replay-final-check')

    def test_full_install_reverses_after_final_syscall_failure(self):
        self.replay('live-replay-failure')

    def test_live_install_without_known_build_profile(self):
        self.replay('live-replay-unknown')

    def test_reverse_preserves_child_hook_installed_during_our_attempt(self):
        self.replay('live-replay-child-race')

    def test_accepts_previously_remapped_page(self):
        self.run_case('anonymous')

    def test_does_not_erase_a_new_owner_at_our_site(self):
        self.run_case('conflict')


if __name__ == '__main__':
    unittest.main()
