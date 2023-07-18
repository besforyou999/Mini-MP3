# Mini-MP3
#### Simple MP3 player ( 미완성 상태 )
#### 
## Features ( 기능 )
### 1. 기기에 저장된 mp3 파일을 읽고 리스트에 올립니다.
#### 
#### 
<img src="https://user-images.githubusercontent.com/74638588/147723587-fbf8c8a2-307c-4bde-a2c8-338493a5fa75.png" width="260" height="500">

* * *

### 2. 듣고 싶은 음악을 터치하면 재생 화면이 시작됩니다.
#### 
#### 
<img src="https://user-images.githubusercontent.com/74638588/147724206-26a2f4fa-06ba-4cac-972a-2376fa162fed.png" width="260" height="500">

* * *

### 3. Play 버튼을 눌러 음악을 재생시키고 Pause 버튼을 눌러 재생을 일시정지 시킵니다.
#### 
#### 
<img src="https://user-images.githubusercontent.com/74638588/147730019-d0469be1-49fa-4caf-8940-82656b498074.png" width="260" height="500">

* * *

### 4. 다른 작업을 하는 도중 알림창으로도 재생/일시정지가 가능합니다.
#### 
#### 
<img src="https://user-images.githubusercontent.com/74638588/147724955-2a752e83-4045-492b-886f-8b08a92927bf.png" width="260" height="500">

* * *

### 5. 음악 재생/일시정지시 알림창 아이콘도 바뀝니다.
#### 
#### 
#### 재생
<img src="https://user-images.githubusercontent.com/74638588/147725359-9c421501-d27d-46cc-ab01-6a7f9e91e075.png" width="260" height="500">

#### 일시정지
<img src="https://user-images.githubusercontent.com/74638588/147725685-e15cc54f-d89f-4b06-a191-1f70379f5bf7.png" width="260" height="500">

* * *

## 구현 기능과 구현 방법

### **1. 음악 목록 만들기**

1. 앱에 MediaStore에 접근 권한을 준다.
2. MediaMetadataRetriever 객체를 생성하여 기기에 저장된 Media에 대한 메타데이터를 읽어온다.
3. FilenameFilter 클래스를 활용해 .mp3  로 끝나는 파일만 배열에 저정한다.
4. MediaStore 객체로 음악 파일을 읽어들인다.
5. 음악 파일들로 listViewItem 클래스 객체를 만든다.
6. ListViewAdapter에 listViewItem 객체들을 추가한다.


### **2. 음악 재생, 일시정지, 다음, 이전**
  
1. 음악 재생

    음악 재생 activity가 생성되면 intent 객체를 생성하여 setAction을 통해 음악 재생 action을 설정하고 startService에 intent을 인자로 전달한다.


2. 음악 일시정지

    사용자가 일시정지 버튼을 누르면 intent 객체를 생성하여 setAction을 통해 음악 일시정지 action을 설정하고 LocalBroadcastManager를 통해 pauseIntent broadcast를 보낸다.

3. 다음, 이전

    목록에서 다음, 이전 음악에 대한 데이터를 읽어온 후 음악 재생 코드 실행

### **3. notification에 재생되는 음악 올리기**

1. RemoteViews 클래스 객체 생성
2. NotificationChannel 클래스 객체 생성. notification에 변경사항이 필요한 경우 이 객체를 통해 notification이 업데이트 됨.
3. BroadcastReciever 클래스 객체 생성하여, notification에서 재생버튼, 일시정지, 이전, 다음 음악 재생 버튼이 클릭되면 intent를 전달 받을 수 있도록 한다.

  
