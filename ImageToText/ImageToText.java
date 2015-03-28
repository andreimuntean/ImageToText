import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.imageio.ImageIO;

/**
 * Converts an image to text art.
 *
 * @author Andrei Muntean
 */
public final class ImageToText
{
    // From the darkest pixel to the brightest one.
    private static char[] shades = "@%Xx+~- ".toCharArray();

    private ImageToText() throws InstantiationException
    {
        // Why C# is superior to Java reason int.MAX_VALUE:
        // Java does not permit static (final abstract) classes.
        // I have to create ugly private constructors.
        throw new InstantiationException();
    }

    private static String getInput(String message)
    {
        System.out.print(message + System.getProperty("line.separator") + "> ");

        return new Scanner(System.in).nextLine();
    }

    private static int[] toPixels(BufferedImage image)
    {
        int width = image.getWidth();
        int height = image.getHeight();

        return image.getRGB(0, 0, width, height, null, 0, width);
    }

    // Gets the alpha value from a pixel.
    private static int getAlpha(int pixel)
    {
        return pixel >>> 24;
    }

    // Gets the red value from a pixel.
    private static int getRed(int pixel)
    {
        return pixel << 8 >>> 24;
    }

    // Gets the green value from a pixel.
    private static int getGreen(int pixel)
    {
        return pixel << 16 >>> 24;
    }

    // Gets the blue value from a pixel.
    private static int getBlue(int pixel)
    {
        return pixel << 24 >>> 24;
    }

    // Gets the brightest pixel.
    private static int getBrightestPixel(int[][] brightnessMap)
    {
        int brightestPixel = 0;

        for (int[] row : brightnessMap)
        {
            for (int pixel : row)
            {
                if (brightestPixel < pixel)
                {
                    brightestPixel = pixel;
                }
            }
        }

        return brightestPixel;
    }

    /**
     * Gets the brightness of a pixel as perceived by humans.
     *
     * @param alpha The alpha value (0 to 255).
     * @param red The red value (0 to 255).
     * @param green The green value (0 to 255).
     * @param blue The blue value (0 to 255).
     *
     * @return The brightness of a pixel as perceived by humans. (0 to 255).
     */
    public static int getBrightness(int alpha, int red, int green, int blue) throws IllegalArgumentException
    {
        if (alpha < 0 || alpha > 255)
        {
            throw new IllegalArgumentException(Integer.toString(alpha));
        }

        if (red < 0 || red > 255)
        {
            throw new IllegalArgumentException(Integer.toString(red));
        }

        if (green < 0 || green > 255)
        {
            throw new IllegalArgumentException(Integer.toString(red));
        }

        if (blue < 0 || blue > 255)
        {
            throw new IllegalArgumentException(Integer.toString(red));
        }

        // The human eye perceives green light as brighter than red and red light as brighter than blue.
        // It is believed that 299/587/114 is the ideal brightness ratio.
        // NOTE: This needs to be revised once non-human intelligent lifeforms are discovered.
        return alpha / 255 * (red * 299 + green * 587 + blue * 114) / 1000;
    }

    /**
     * Gets the brightness of each pixel as perceived by humans from a specified image.
     *
     * @param bufferedImage An image.
     *
     * @return The brightness of each pixel as perceived by humans from a specified image.
     */
    public static int[][] getBrightnessMap(BufferedImage bufferedImage)
    {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        int[] pixels = toPixels(bufferedImage);
        int[][] brightnessMap = new int[height][width];

        for (int y = 0; y < height; ++y)
        {
            for (int x = 0; x < width; ++x)
            {
                int currentPixel = pixels[y * width + x];
                int alpha = getAlpha(currentPixel);
                int red = getRed(currentPixel);
                int green = getGreen(currentPixel);
                int blue = getBlue(currentPixel);

                brightnessMap[y][x] = getBrightness(alpha, red, green, blue);
            }
        }

        return brightnessMap;
    }

    /**
     * Converts an image to text art.
     *
     * @param source Image file path.
     * @param destination Destination file path.
     *
     * @throws NullPointerException Input source is not a valid image.
     * @throws Exception Conversion failed.
     */
    public static void convert(String source, String destination) throws NullPointerException, Exception
    {
        BufferedImage bufferedImage = ImageIO.read(new File(source));
        int[][] brightnessMap = getBrightnessMap(bufferedImage);
        int brightestPixel = getBrightestPixel(brightnessMap);

        try (PrintWriter writer = new PrintWriter(destination))
        {
            for (int[] row : brightnessMap)
            {
                for (int pixel : row)
                {
                    int shadeIndex = pixel * shades.length / (brightestPixel + 1);

                    writer.print(shades[shadeIndex]);
                }

                writer.println();
            }
        }
    }

    /**
     * Runs the program.
     *
     * @param args Image and destination file paths. Can be empty.
     */
    public static void main(String[] args)
    {
        try
        {
            if (args != null && args.length >= 2)
            {
                // Handles additional commands.
                for (int argIndex = 2; argIndex < args.length; ++argIndex)
                {
                    // Gets the command.
                    String command = args[argIndex].toLowerCase();

                    switch (command)
                    {
                        // Inverts the colors.
                        case "invert":
                            shades = new StringBuilder(new String(shades)).reverse().toString().toCharArray();

                            break;
                    }
                }

                // The two file paths have been provided as command line arguments.
                convert(args[0], args[1]);
            }
            else
            {
                // Asks for user input.
                String source = getInput("Image source path:");
                String destination = getInput("File destination path:");

                convert(source, destination);
            }

            System.out.println("Successfully converted image.");
        }
        catch (NullPointerException exception)
        {
            System.err.println("Input source is not a valid image.");
        }
        catch (Exception exception)
        {
            System.err.println("An error has occurred: " + exception.getMessage());
        }
    }
}