## Windowsƽ̨�������

### ���蹤��

* JDK 17 [����](https://www.oracle.com/cn/java/technologies/downloads/#jdk17-windows)
* WiX Toolset 3.x [����](https://github.com/wixtoolset/wix3/releases)
* JavaFX-SDK 17 [����](https://gluonhq.com/products/javafx/)
* JavaFX-jmods 17 [����](https://gluonhq.com/products/javafx/)

### ׼������

* ��װJDK�����û�������
* ��װWiX�����û�������
* ��ѹJavaFX-SDK������Ŀ¼
* ��ѹJavaFX-jmods������Ŀ¼

### ��ʼ���

#### �����ִ��Jar�ļ�

* ��IDEA�����artifact��Project Structure -> Artifacts -> Add JAR -> from modules with dependencies...��
* ѡ�����࣬����`MANIFEST.MF`�ļ���`src\main`Ŀ¼��
* ���JavaFX-SDK��`bin`Ŀ¼�����е�dll�ļ���Output Layout -> Add Copy of File��
* ʹ��IDEA����Jar��Build -> Build Artifacts��

#### ���Windows��װ����

�Ӵ���õ���Jar����Ŀ¼�д�Windows�����У�ִ��`jpackage`���Ҫ��JDK 14�����ϣ���

��ʽ��

`jpackage -t [����] -n [��������] -i [����Ŀ¼] -d [���Ŀ¼] --main-jar [��jar�������Ŀ¼��·��] --main-class [������] -p [jmod·��] --add-modules [ģ����] --win-shortcut --win-menu`

ʾ����

`jpackage -t msi -n video-editor -i . -d D:\TEMP --main-jar .\video-editor.jar --main-class com.example.videoeditor.AppLauncher -p "C:\MyPrograms\Java\javafx-jmods-17.0.10" --add-modules javafx.controls,javafx.fxml --win-shortcut --win-menu`

ע�⣺ ����Ŀ¼�µ������ļ�����������������Ҫȷ������Ŀ¼���������޹��ļ���
