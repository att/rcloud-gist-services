base:
  pkgrepo.managed:
    - humanname: MARutter PPA
    - name: ppa:marutter/rrutter

rcloud-dependencies:
  pkg.installed:
    - pkgs:
      - openjdk-7-jdk
      - gcc
      - g++
      - gfortran
      - libcairo-dev
      - libreadline-dev
      - libxt-dev
      - libjpeg-dev
      - libicu-dev
      - libssl-dev
      - libcurl4-openssl-dev
      - subversion
      - git
      - automake
      - make
      - libtool
      - libtiff-dev
      - gettext
      - redis-server
      - rsync
      - r-base-dev
#      - nodejs
#      - npm

rcloud-deploy:
  archive.extracted:
    - name: /opt/rcloud
    - source: https://github.com/att/rcloud/archive/1.7.tar.gz
    - source_hash: md5=643eff16f448bf1306cbcd08930cfb99
    - source_hash_update: True
  cmd.run:
    - name: ./scripts/bootstrapR.sh
    - cwd: /opt/rcloud/rcloud-1.7/
    - creates: /opt/rcloud/rcloud-1.7/conf/rcloud.conf
