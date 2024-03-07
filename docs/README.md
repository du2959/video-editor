## Windows平台打包方法

### 所需工具

* JDK 17 [下载](https://www.oracle.com/cn/java/technologies/downloads/#jdk17-windows)
* WiX Toolset 3.x [下载](https://github.com/wixtoolset/wix3/releases)
* JavaFX-SDK 17 [下载](https://gluonhq.com/products/javafx/)
* JavaFX-jmods 17 [下载](https://gluonhq.com/products/javafx/)

### 准备工作

* 安装JDK并配置环境变量
* 安装WiX并配置环境变量
* 解压JavaFX-SDK到任意目录
* 解压JavaFX-jmods到任意目录

### 开始打包

#### 打包可执行Jar文件

* 在IDEA中添加artifact（Project Structure -> Artifacts -> Add JAR -> from modules with dependencies...）
* 选择主类，创建`MANIFEST.MF`文件到`src\main`目录下
* 添加JavaFX-SDK的`bin`目录下所有的dll文件（Output Layout -> Add Copy of File）
* 使用IDEA生成Jar（Build -> Build Artifacts）

#### 打包Windows安装程序

从打包得到的Jar所在目录中打开Windows命令行，执行`jpackage`命令（要求JDK 14或以上）。

格式：

`jpackage -t [类型] -n [程序名称] -i [输入目录] -d [输出目录] --main-jar [主jar相对输入目录的路径] --main-class [启动类] -p [jmod路径] --add-modules [模块名] --win-shortcut --win-menu`

示例：

`jpackage -t msi -n video-editor -i . -d D:\TEMP --main-jar .\video-editor.jar --main-class com.example.videoeditor.AppLauncher -p "C:\MyPrograms\Java\javafx-jmods-17.0.10" --add-modules javafx.controls,javafx.fxml --win-shortcut --win-menu`

注意： 输入目录下的所有文件都会打包进来，所以要确保输入目录不含其他无关文件。
