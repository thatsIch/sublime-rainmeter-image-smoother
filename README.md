[![Build status](https://ci.appveyor.com/api/projects/status/mmsc7ltsi4dq3cw2/branch/master?svg=true)](https://ci.appveyor.com/project/thatsIch/sublime-rainmeter-image-smoother/branch/master)

About
=====
This is a simple tool which uses [OpenCV](http://opencv.org/) through its Java Binding [JavaCV](https://github.com/bytedeco/javacv) to generate a smoothed shadow on each image.

If you would pass this program an input image like

![Input Image](https://cloud.githubusercontent.com/assets/2210496/20466360/477b74a0-af72-11e6-8279-fcd6fb8999f2.png)

this would output

![Output Image](https://cloud.githubusercontent.com/assets/2210496/20466368/5e35aff8-af72-11e6-8d7b-93b3184efd43.png)

The ground truth image (how it is supposed to look like) was given by [@merlinthered](https://github.com/merlinthered)

![Ground Truth](https://cloud.githubusercontent.com/assets/2210496/20466364/559b668a-af72-11e6-8dfc-dbd7e03d5825.png)

Building
========
    mvnw clean install

(This will install a Maven wrapper which requires you not to download and install Maven yourself)

Running
=======
    java -jar target/sublime-rainmeter-image-smoother-1.0.jar "path/to/input.png" "path/to/input2.png"
    
results into two files in the same directories as `input` and `input2` with the postfix `-shaded`.
