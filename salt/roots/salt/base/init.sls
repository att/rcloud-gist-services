system-uptodate:
  pkg.uptodate:
    - name: Ensure system is up to date.
    - refresh: True

iptables:
  service.dead:
    - enable: False
