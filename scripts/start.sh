#!/usr/bin/env bash
set -e

ABSPATH=$(readlink -f $0)
ABSDIR=$(dirname $ABSPATH)
source ${ABSDIR}/profile.sh   # import profile.sh

REPOSITORY=/home/ec2-user/app/step3
PROJECT_NAME=soob_backend

pwd # debug
cd $REPOSITORY # 실행위치로 이동 # 내가따로적음
pwd # debug

echo "> Build 파일 복사"
echo "> cp $REPOSITORY/zip/*.jar $REPOSITORY/"
cp $REPOSITORY/zip/*.jar $REPOSITORY/

echo "> 새 어플리케이션 배포"
JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)    # jar 이름 꺼내오기
echo "> JAR Name: $JAR_NAME"

echo "> $JAR_NAME 에 실행권한 추가"
chmod +x $JAR_NAME

echo "> $JAR_NAME 실행"
IDLE_PROFILE=$(find_idle_profile)
echo ${IDLE_PROFILE} # debug

echo "> $JAR_NAME 를 profile=$IDLE_PROFILE 로 실행합니다."
nohup java -jar $JAR_NAME \
    -Dspring.profiles.active=$IDLE_PROFILE \
    > $REPOSITORY/nohup.out 2>&1 &
