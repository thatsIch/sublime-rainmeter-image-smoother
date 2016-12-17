package de.thatsich.rainmeter.image.smooth;

import org.bytedeco.javacpp.opencv_core;

import java.io.File;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_imgcodecs.IMREAD_UNCHANGED;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RGB2RGBA;
import static org.bytedeco.javacpp.opencv_imgproc.blur;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

/**
 * @author thatsIch (thatsich@mail.de)
 * @version 1.0-SNAPSHOT 20.11.2016
 * @since 1.0-SNAPSHOT
 */
public class Smoother {
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Requires at least one argument: <path to image>+");
			return;
		}

		for (final String filename : args) {
			final File inputFile = new File(filename);
			final Mat input = Loader.loadOrExit(inputFile, IMREAD_UNCHANGED);
			if (input.channels() == 3) {
				cvtColor(input, input, CV_RGB2RGBA, 4);
			}

			// copy data over to create a new Black Shadow
			final opencv_core.Size size = input.size();
			final Mat shadow = new Mat(size, 24, new opencv_core.Scalar(0, 0, 0, 255));

			// make the result in total 16px wider
			final opencv_core.Size resultSize = new opencv_core.Size(size.width() + 16, size.height() + 16);
			final Mat result = new Mat(resultSize, 24, new opencv_core.Scalar(0, 0, 0, 0));

			// copy the black shadow over
			final Mat shadowROI = result.apply(new opencv_core.Rect(8, 8, size.width(), size.height()));
			shadow.copyTo(shadowROI);

			// blur the shadow
			final opencv_core.Size kernelSize = new opencv_core.Size(10, 10);
			blur(result, result, kernelSize);

			// copy in the original image offset by 6,6 topleft
			final Mat imageROI = result.apply(new opencv_core.Rect(6, 6, size.width(), size.height()));
			input.copyTo(imageROI);

			// save for comparison sake
			final File parentFile = inputFile.getParentFile();
			final String strippedFileName = getFileNameWithoutExtension(inputFile);

			final File outputFile = new File(parentFile, strippedFileName + "-shaded.png");
			Loader.save(outputFile, result);
		}
	}

	private static String getFileNameWithoutExtension(File file) {
		final String name = file.getName();
		final int pos = name.lastIndexOf(".");
		if (pos > 0) {
			return name.substring(0, pos);
		}

		throw new RuntimeException("Expected file with an extension but got '" + name + "' through '" + file + "'");
	}
}
