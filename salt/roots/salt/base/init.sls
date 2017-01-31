#*******************************************************************************
# Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
#
# SPDX-License-Identifier:   MIT
#
#******************************************************************************/
system-uptodate:
  pkg.uptodate:
    - name: Ensure system is up to date.
    - refresh: True

iptables:
  service.dead:
    - enable: False
