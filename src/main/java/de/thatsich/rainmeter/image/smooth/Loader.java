package de.thatsich.rainmeter.image.smooth;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import javax.swing.*;
import java.io.File;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;

/**
 * TODO add description
 * <p>
 * TODO add meaning
 * <p>
 * TODO add usage
 *
 * @author thatsIch (thatsich@mail.de)
 * @version 1.0-SNAPSHOT 20.11.2016
 * @since 1.0-SNAPSHOT
 */
public class Loader {

	static void show(Mat mat, String title) {
		final OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

		CanvasFrame canvas = new CanvasFrame(title, 1);
		canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		canvas.showImage(converter.convert(mat));
	}

	static Mat loadOrExit(File file, int flags) {
		final Mat image = imread(file.getAbsolutePath(), flags);
		if (image.empty()) {
			System.out.println("Could not load image: " + file.getAbsolutePath());
			System.exit(1);
		}

		return image;
	}

	static void save(File file, Mat image) {
		imwrite(file.getAbsolutePath(), image);
	}
}
