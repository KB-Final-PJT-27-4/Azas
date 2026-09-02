#!/usr/bin/env bash
set -Eeuo pipefail

incoming_war="/tmp/azas-ROOT.war"
webapps_dir="/opt/tomcat/webapps"
current_war="${webapps_dir}/ROOT.war"
exploded_app="${webapps_dir}/ROOT"
backup_dir="/opt/azas-backups"
deploy_stamp="$(date +%Y%m%d%H%M%S)"
backup_war="${backup_dir}/ROOT.war.${deploy_stamp}"
has_backup=false

exec 9>/var/lock/azas-backend-deploy.lock
flock -n 9 || {
  echo "Another backend deployment is in progress."
  exit 1
}

test -f "$incoming_war"
test ! -L "$incoming_war"
test -s "$incoming_war"
test "$(stat -c '%U' "$incoming_war")" = "actions"
test -d "$webapps_dir"
mkdir -p "$backup_dir"

if test -f "$current_war"; then
  cp -p "$current_war" "$backup_war"
  has_backup=true
fi

rollback() {
  echo "Deployment failed. Restoring the previous WAR."
  systemctl stop azas-tomcat || true

  if test "$has_backup" = true && test -f "$backup_war"; then
    install -o tomcat -g tomcat -m 0640 "$backup_war" "$current_war"
  else
    rm -f -- "$current_war"
  fi

  if test -d "$exploded_app"; then
    rm -rf -- "$exploded_app"
  fi

  systemctl start azas-tomcat || true
}
trap rollback ERR

systemctl stop azas-tomcat

if test -d "$exploded_app"; then
  rm -rf -- "$exploded_app"
fi

install -o tomcat -g tomcat -m 0640 "$incoming_war" "$current_war"
systemctl start azas-tomcat

health_ok=false
for _ in $(seq 1 45); do
  if curl -fsS http://127.0.0.1:8080/api/v1/health \
    | grep -q '"status":"UP"'; then
    health_ok=true
    break
  fi
  sleep 2
done

if test "$health_ok" != true; then
  echo "Internal health check failed."
  exit 1
fi

systemctl is-active --quiet azas-tomcat
rm -f -- "$incoming_war"
trap - ERR

echo "Backend deployment completed successfully."
