#*******************************************************************************
# Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
#
# SPDX-License-Identifier:   MIT
#
#******************************************************************************/

#sudo salt-call --local -l info state.highstate
base:
  '*':
    - base
    - rcloud
