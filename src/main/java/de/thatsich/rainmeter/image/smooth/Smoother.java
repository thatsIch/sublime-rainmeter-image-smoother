package de.thatsich.rainmeter.image.smooth;

import org.apache.commons.io.FileUtils;
import org.bytedeco.javacpp.opencv_core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

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

		final LocalTime totalStartTime = printWithTimeStamp("Start processing of '"+args.length+"' images\n");
		for (final String filename : args) {
			final LocalTime start = printWithTimeStamp("Processing '" + filename + "' out of '" + args.length + "'");

			final File inputFile = new File(filename);
			final Mat input = Loader.loadOrExit(inputFile, IMREAD_UNCHANGED);
			if (input.channels() == 3) {
				cvtColor(input, input, CV_RGB2RGBA, 4);
			}
			printWithTimeStamp("\t- Loaded '" + filename + "'");

			// copy data over to create a new Black Shadow
			final opencv_core.Size size = input.size();
			final Mat shadow = new Mat(size, 24, new opencv_core.Scalar(0, 0, 0, 255));
			printWithTimeStamp("\t- Created black shadow with size " + getSizeString(size));

			// make the result in total 16px wider
			final opencv_core.Size resultSize = new opencv_core.Size(size.width() + 16, size.height() + 16);
			final Mat result = new Mat(resultSize, 24, new opencv_core.Scalar(0, 0, 0, 0));
			printWithTimeStamp("\t- Enlarged image for shifting operation to size " + getSizeString(resultSize));

			// copy the black shadow over
			final Mat shadowROI = result.apply(new opencv_core.Rect(8, 8, size.width(), size.height()));
			shadow.copyTo(shadowROI);
			printWithTimeStamp("\t- Copied over the black shadow");

			// blur the shadow
			final opencv_core.Size kernelSize = new opencv_core.Size(10, 10);
			blur(result, result, kernelSize);
			printWithTimeStamp("\t- Blurred shadow with kernel size " + getSizeString(kernelSize));

			// copy in the original image offset by 6,6 topleft
			final Mat imageROI = result.apply(new opencv_core.Rect(6, 6, size.width(), size.height()));
			input.copyTo(imageROI);
			printWithTimeStamp("\t- Copied over the original image");

			// save for comparison sake
			final File parentFile = inputFile.getParentFile();
			final String strippedFileName = getFileNameWithoutExtension(inputFile);

			final File outputFile = new File(parentFile, strippedFileName + "-shaded.png");
			Loader.save(outputFile, result);
			printWithTimeStamp("\t- Saved shaded image to '" + outputFile + "'");

			try {
				final File qFile = quantifyPngFile(outputFile);
				final long initialSize = outputFile.length();
				final long compressedSize = qFile.length();
				final double sizePercentage = ((double) compressedSize / initialSize) * 100;
				final String formattedPercentage = String.format("%.2f", sizePercentage);

				printWithTimeStamp("\t- Quantified image '" + qFile + "' to '" + formattedPercentage + "%'");
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}

			final LocalTime end = LocalTime.now();
			final long processingTime = start.until(end, ChronoUnit.MILLIS);
			printWithTimeStamp("Whole process for '"+filename+"' took '" + processingTime + "'ms\n");
		}

		final LocalTime finalEndTime = LocalTime.now();
		final long totalMSDiff = totalStartTime.until(finalEndTime, ChronoUnit.MILLIS);
		printWithTimeStamp("Shading of '"+args.length+"' images took '"+totalMSDiff+"'ms");
	}

	private static LocalTime lastTimeStamp = null;
	private static LocalTime printWithTimeStamp(final String message) {
		final LocalTime now = LocalTime.now();
		final String offset = (lastTimeStamp != null) ? " +" + String.format("% 5d", lastTimeStamp.until(now, ChronoUnit.MILLIS)) : "";
		lastTimeStamp = now;
		System.out.println("["+now+offset+"] " + message);

		return now;
	}

	private static String getSizeString(opencv_core.Size size) {
		return "'("+size.width()+", "+size.height()+")'";
	}

	private static String getFileNameWithoutExtension(File file) {
		final String name = file.getName();
		final int pos = name.lastIndexOf(".");
		if (pos > 0) {
			return name.substring(0, pos);
		}

		throw new RuntimeException("Expected file with an extension but got '" + name + "' through '" + file + "'");
	}

	private static File extractQuantifyer() throws IOException {
		final URL inputUrl = Smoother.class.getResource("/pngquant.exe");
		final File tempFile = File.createTempFile("pngquant", ".exe");
		FileUtils.copyURLToFile(inputUrl, tempFile);

		return tempFile;
	}

	private static File quantifyPngFile(final File pngFile) throws IOException, InterruptedException {
		final File quantifyer = extractQuantifyer();
		ProcessBuilder pb = new ProcessBuilder();
		pb.inheritIO();
		pb.command(quantifyer.getAbsolutePath(), "--force", "--verbose", "--ordered", "--speed=1", "--quality=50-90", pngFile.getAbsolutePath());
		Process p = pb.start();
		int result = p.waitFor();
		if (result != 0) {
			System.exit(result);
		}

		final String parent = pngFile.getParent();
		final File outputFile = new File(parent, getFileNameWithoutExtension(pngFile) + "-or8.png");

		return outputFile;
	}
}
