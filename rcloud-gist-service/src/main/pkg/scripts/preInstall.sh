# create the rcloudgistservice user and rcloudgistservice
/usr/sbin/useradd -c "RCloud Gist Service" \
    -r -d /opt/rcloud-gist-service rcloudgistservice 2>/dev/null || :

LOG_DIR=/var/log/rcloud-gist-service
mkdir -p $LOG_DIR
chown rcloudgistservice:root $LOG_DIR
