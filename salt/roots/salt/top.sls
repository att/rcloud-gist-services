#sudo salt-call --local -l info state.highstate
base:
  '*':
    - base
    - rcloud
