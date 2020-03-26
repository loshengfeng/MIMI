# MiMi Video

#### GitLab CI/CD
- Docker Build
  ```
  docker build --build-arg ANDROID_SDK_TOOLS=4333796 --build-arg ANDROID_COMPILE_SDK=29 --build-arg ANDROID_BUILD_TOOLS=29.0.3 -t registry.silkrode.com.tw/team_mobile/mimi:[TAG] .
  ```
      
- Docker Push
  ```
  docker push registry.silkrode.com.tw/team_mobile/mimi:[TAG]
  ```
