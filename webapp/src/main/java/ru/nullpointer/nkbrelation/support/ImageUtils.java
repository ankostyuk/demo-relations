package ru.nullpointer.nkbrelation.support;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.text.AttributedString;

import org.imgscalr.Scalr;

/**
 * @author ankostyuk
 * @author "Anton Brok-Volchansky <re6exp@gmail.com>"
 */
public final class ImageUtils {

    private static final Color CUTOFF_LINE_COLOR = new Color(128, 128, 128);
    public static final int IMAGE_TYPE = BufferedImage.TYPE_INT_ARGB;
    public static final int WHITE_ARGB = 0xFFFFFFFF;

    private ImageUtils() {
    }

    public static BufferedImage trim(BufferedImage image, int whitespaceARGB) {
        int x1 = Integer.MAX_VALUE;
        int y1 = Integer.MAX_VALUE;
        int x2 = 0;
        int y2 = 0;

        boolean trim = false;

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int argb = image.getRGB(x, y);
                if (argb != whitespaceARGB) {
                    x1 = Math.min(x1, x);
                    y1 = Math.min(y1, y);
                    x2 = Math.max(x2, x);
                    y2 = Math.max(y2, y);

                    trim = true;
                }
            }
        }

        if (trim) {
            ColorModel cm = image.getColorModel();

            WritableRaster wr = image.getRaster();

            wr = wr.createWritableChild(x1, y1, x2 - x1 + 1, y2 - y1 + 1, 0, 0, null);

            return new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), null);
        } else {
            return image;
        }
    }

    /**
     * TODO: See https://code.google.com/p/java-image-scaling/wiki/Introduction
     * Change on using the library above here to improve performance!
     *
     * @param image
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static BufferedImage resizeOld(BufferedImage image, int newWidth, int newHeight) {
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, image.getType());

        Graphics2D g = resizedImage.createGraphics();

        g.setComposite(AlphaComposite.Src);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(image, 0, 0, newWidth, newHeight, null);

        g.dispose();

        return resizedImage;
    }

    public static BufferedImage resize(BufferedImage image, int newWidth, int newHeight) {
        BufferedImage resizedImage = Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, newWidth, newHeight, Scalr.OP_ANTIALIAS);
        return resizedImage;
    }

    /**
     * Разбиение изображения на фрагменты
     *
     * @param sourceImage - исходное изображение
     * @param chunkWidth - ширина фрагмента
     * @param chunkHeight - высота фрагмента
     * @return - массив фрагментов изображения
     * @throws IOException
     * @throws FontFormatException
     */
    public static BufferedImage[] split(BufferedImage sourceImage, int chunkWidth, int chunkHeight) {
        int wholeColumns = sourceImage.getWidth() / chunkWidth;
        int partColumn = sourceImage.getWidth() % chunkWidth;
        int cols = wholeColumns + (partColumn == 0 ? 0 : 1);

        int wholeRows = sourceImage.getHeight() / chunkHeight;
        int partRow = sourceImage.getHeight() % chunkHeight;
        int rows = wholeRows + (partRow == 0 ? 0 : 1);

        int chunks = rows * cols;
        BufferedImage imgs[] = new BufferedImage[chunks];

        int count = 0;
        int imgType = sourceImage.getType();

        int sx1;
        int sy1;
        int sx2;
        int sy2;

        int width;
        int height;

        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                sx1 = chunkWidth * y;
                sy1 = chunkHeight * x;

                if (x != (rows - 1)) {
                    sy2 = chunkHeight * x + chunkHeight;
                    height = chunkHeight;
                } else {
                    sy2 = chunkHeight * x + partRow;
                    height = partRow;
                }

                if (y != (cols - 1)) {
                    sx2 = chunkWidth * y + chunkWidth;
                    width = chunkWidth;
                } else {
                    sx2 = chunkWidth * y + partColumn;
                    width = partColumn;
                }

                imgs[count] = new BufferedImage(width, height, imgType);

                Graphics2D gr = imgs[count].createGraphics();
                gr.drawImage(sourceImage,//
                        0, // dx1
                        0, // dy1

                        width, // dx2
                        height, // dy2

                        sx1, // sx1
                        sy1, // sy1

                        sx2, // sx2
                        sy2, // sy2

                        null);

                gr.dispose();

                count++;
            }
        }

        return imgs;
    }

    /**
     * Изображение-превью для набора нарезанных изображений, полученных из
     * изображения графа
     *
     * @param sourceImage {@link BufferedImage}
     * @param chunkWidth int ширина нарезаемых фрагментов
     * @param chunkHeight int высота нарезаемых фрагментов
     * @param rows int количество строк
     * @param columns int количество столбцов
     * @param imgType
     * @param firstNumberOfChunkToSignFrom
     * @return
     * @throws FontFormatException
     * @throws IOException
     */
    public static BufferedImage getPreview(BufferedImage sourceImage, String fontName, int chunkWidth, int chunkHeight, int imgType, int firstNumberOfChunkToSignFrom) {
        int wholeColumns = sourceImage.getWidth() / chunkWidth;
        int partColumn = sourceImage.getWidth() % chunkWidth;
        int columns = wholeColumns + (partColumn == 0 ? 0 : 1);

        int wholeRows = sourceImage.getHeight() / chunkHeight;
        int partRow = sourceImage.getHeight() % chunkHeight;
        int rows = wholeRows + (partRow == 0 ? 0 : 1);

        // Получить изображение холста - того пространства, что составляется полным набором chunk'ов.
        int canvasWidth = chunkWidth * columns;
        int canvasHeight = chunkHeight * rows;

        double thumbnailImageWidth = ((double) canvasWidth);
        double thumbnailImageHeight = ((double) canvasHeight);

        // изображение превью
        BufferedImage thumbnailImage = new BufferedImage((int) Math.round(thumbnailImageWidth), (int) Math.round(thumbnailImageHeight), imgType);

        // закрасить бекграунд белым цветом
        Graphics2D gr = thumbnailImage.createGraphics();
        gr.setColor(Color.white);
        gr.setBackground(Color.white);
        gr.clearRect(0, 0, thumbnailImage.getWidth(), thumbnailImage.getHeight());
        gr.dispose();

        gr = thumbnailImage.createGraphics();
        gr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        gr.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // вписываем граф объектов с одновременным масштабированием
        gr.drawImage(sourceImage,//
                0, // dx1
                0, // dy1
                sourceImage.getWidth(), // dx2
                sourceImage.getHeight(), // dy2
                0, // sx1
                0, // sy1
                sourceImage.getWidth(), // sx2
                sourceImage.getHeight(), // sy2
                null,//
                null);
        gr.dispose();

        // Отрисовка линий отреза.
        // Размеры фрагмента изображения на изображении превью графа,
        // пропорциональные полному фрагменту графа, выводимому на одном листе.
        int tumbnailedChunkWidth = thumbnailImage.getWidth() / columns;
        int tumbnailedChunkHeight = thumbnailImage.getHeight() / rows;

        float[] dashPattern = {3, 2};
        gr = thumbnailImage.createGraphics();
        gr.setColor(CUTOFF_LINE_COLOR);
        gr.setStroke(new BasicStroke(0.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, 0));

        // вертикальные линии
        for (int i = 1; i < columns; i++) {
            gr.drawLine(//
                    tumbnailedChunkWidth * i,//
                    0,//
                    tumbnailedChunkWidth * i,//
                    thumbnailImage.getHeight());
        }

        // горизонтальные линии
        for (int i = 1; i < rows; i++) {
            gr.drawLine(//
                    0,// x1
                    i * tumbnailedChunkHeight,// y1
                    thumbnailImage.getWidth(),// x2
                    i * tumbnailedChunkHeight// y2
            );
        }

        try {
            // номер фрагмента
            writeNumbersOverChunks(gr, fontName, tumbnailedChunkWidth, tumbnailedChunkHeight, rows, columns, firstNumberOfChunkToSignFrom);
        } catch (FontFormatException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return thumbnailImage;
    }

    /**
     * Центрирование порядкового номера по центру отображений фрагментов в
     * шаблоне нарезки изображения графа
     *
     * @param gr {@link Graphics2D} графический контекст
     * @param tumbnailedChunkWidth int
     * @param tumbnailedChunkHeight int
     * @param rows int
     * @param columns int
     * @throws FontFormatException
     * @throws IOException
     */
    private static void writeNumbersOverChunks(Graphics2D gr, String fontName, int tumbnailedChunkWidth, int tumbnailedChunkHeight, int rows, int columns, int firstNumberOfChunkToSignFrom) throws FontFormatException, IOException {
        double fontSizeD = tumbnailedChunkHeight > tumbnailedChunkWidth ? ((double) tumbnailedChunkHeight / 4) : ((double) tumbnailedChunkWidth / 4);
        int fontSize = (int) Math.round(fontSizeD);

        Font font = new Font(fontName, Font.PLAIN, fontSize);
        FontMetrics metrics = gr.getFontMetrics(font);

        for (int i = 0, number = firstNumberOfChunkToSignFrom; i < rows; i++) {
            for (int j = 0; j < columns; j++, number++) {
                String numberOfChunk = String.valueOf(number);

                AttributedString attributedString = new AttributedString(numberOfChunk);
                attributedString.addAttribute(TextAttribute.FONT, font);
                attributedString.addAttribute(TextAttribute.SIZE, fontSize);

                FontMetrics fm = gr.getFontMetrics(font);
                java.awt.geom.Rectangle2D rect = metrics.getStringBounds(numberOfChunk, gr);

                int textHeight = (int) (rect.getHeight());
                int textWidth = (int) (rect.getWidth());

                // Center text horizontally and vertically
                int x = ((tumbnailedChunkWidth - textWidth) / 2) + j * tumbnailedChunkWidth;
                int y = ((tumbnailedChunkHeight - textHeight) / 2 + fm.getAscent()) + (i * tumbnailedChunkHeight);

                gr.drawString(attributedString.getIterator(), x, y);
            }
        }
    }

    public static boolean isPortrait(BufferedImage image) {
        boolean retVal = (image.getHeight() / image.getWidth()) >= 1;
        return retVal;
    }
}
