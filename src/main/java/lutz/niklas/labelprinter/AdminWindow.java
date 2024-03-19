package lutz.niklas.labelprinter;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXTextField;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

public class AdminWindow implements Initializable {

    @FXML
    public Button print;
    @FXML
    public Button preview;
    @FXML
    public JFXTextField input;
    @FXML
    public JFXTextField height;
    @FXML
    public JFXTextField width;
    @FXML
    public CheckBox enableEncode;
    @FXML
    public CheckBox useStandardSize;
    @FXML
    public CheckBox checkValidity;
    private double x = 0,y = 0;
    @FXML
    public AnchorPane admin;
    @FXML
    public ImageView barcodeViewer;
    private Stage stage;

    private final String[] prefixes = new String[]{"30", "32", "34", "40", "42", "44", "90"};

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        admin.setOnMousePressed(mouseEvent -> {
            x = mouseEvent.getSceneX();
            y = mouseEvent.getSceneY();
        });

        admin.setOnMouseDragged(mouseEvent -> {
            stage.setX(mouseEvent.getScreenX() - x);
            stage.setY(mouseEvent.getScreenY() - y);
        });
    }


    /**
     * Creates the preview image of the barcode to be printed.
     * It will take the input from the textfield and generate the barcode based on that.
     * @throws IOException if the BufferedReader in the validity check fails.
     */
    @FXML
    private void preview() throws IOException {
        String prefix = input.getText().substring(0, 2);
        String code = enableEncode.isSelected() ? Encoder.encode(input.getText()) : input.getText();
        Code128Writer writer = new Code128Writer();
        boolean check = checkValidity.isSelected();
        boolean valid = checkValid(input.getText());
        boolean customSize = !useStandardSize.isSelected();
        double imgWidth;
        double imgHeight;
        try {
            imgWidth = customSize ? Double.parseDouble(width.getText()) : 0;
            imgHeight = customSize ? Double.parseDouble(height.getText()) : 0;
        } catch (NumberFormatException e) {
            Label label = new Label("FEHLER: Höhe und Breite müssen ganzzahlige Werte sein.");
            styleError(new JFXSnackbar(admin), label);
            return;
        }
        if (check && valid) {
            Label label = new Label("FEHLER: Der Code ist nicht in der Datenbank.");
            styleError(new JFXSnackbar(admin), label);
            return;
        }
        BufferedImage img = getBufferedImage(prefix);
        if (customSize && (imgHeight < 15 || imgHeight > 100)) {
            Label label = new Label("FEHLER: Das Etikett muss zwischen 15mm und 100mm hoch sein.");
            styleError(new JFXSnackbar(admin), label);
            return;
        } else if (customSize && imgWidth > 101) {
            Label label = new Label("FEHLER: Das Etikett darf höchstens 101mm breit sein.");
            styleError(new JFXSnackbar(admin), label);
            return;
        }
        BitMatrix matrix = writer.encode(code, BarcodeFormat.CODE_128, img.getWidth(), img.getHeight()-30);
        BufferedImage barcode = MatrixToImageWriter.toBufferedImage(matrix);
        if (customSize && imgWidth < barcode.getWidth()) {
            Label label = new Label("FEHLER: Der Barcode ist zu breit für das Etikett.");
            styleError(new JFXSnackbar(admin), label);
            return;
        }
        drawBarcode(img, barcode);
        centerImage();
        print.setDisable(false);
    }

    /**
     * Adds two BufferedImages together, drawing the second argument on top of the first one.
     * @param img The image to be used as the base
     * @param barcode The image to draw on to the base
     */
    private void drawBarcode(BufferedImage img, BufferedImage barcode) {
        Graphics2D graphics = img.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, img.getWidth(), img.getHeight());
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F));
        graphics.drawImage(barcode, (img.getWidth()- barcode.getWidth())/2, 0, null);
        graphics.setColor(Color.BLACK);
        graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setFont(new Font("Arial", Font.PLAIN, 24));
        FontMetrics metrics = graphics.getFontMetrics();
        int width = metrics.stringWidth(input.getText());
        graphics.drawString(input.getText(), (img.getWidth()-width)/2, img.getHeight() - 5);
        graphics.dispose();
        WritableImage wr = new WritableImage(img.getWidth(), img.getHeight());
        PixelWriter pw = wr.getPixelWriter();
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                pw.setArgb(x, y, img.getRGB(x, y));
            }
        }
        barcodeViewer.setImage(new ImageView(wr).getImage());
    }

    /**
     * Creates and returns a BufferedImage with the specified or default sizes, depending on the selected checkboxes.
     * @param prefix The prefix of the barcode to use. This is only checked when the standard sizes are used.
     * @return an empty BufferedImage with the given size.
     */
    private BufferedImage getBufferedImage(String prefix) {
        BufferedImage img;
        img = useStandardSize.isSelected() ?
                Arrays.asList(prefixes).contains(prefix) ?
                        new BufferedImage((int) Encoder.fromCMtoPPI(7), (int) Encoder.fromCMtoPPI(8), BufferedImage.TYPE_INT_ARGB)
                        : new BufferedImage((int) Encoder.fromCMtoPPI(9), (int) Encoder.fromCMtoPPI(6), BufferedImage.TYPE_INT_ARGB)
                        : new BufferedImage((int) Encoder.fromCMtoPPI(Double.parseDouble(width.getText()) / 10),
                            (int) Encoder.fromCMtoPPI(Double.parseDouble(height.getText()) / 10), BufferedImage.TYPE_INT_ARGB);
        return img;
    }

    /**
     * Helper method to correctly style the error toasts.
     * @param error the snackbar to display the message on
     * @param label the label that will be displayed on the textbox
     */
    private void styleError(JFXSnackbar error, Label label) {
        label.setPrefWidth(admin.getWidth());
        label.setAlignment(Pos.CENTER);
        label.setStyle("""
                -fx-background-color: #ffcd48;
                -fx-background-radius: 25;
                -fx-text-fill: #000000;
                -fx-font-family: Arial;
                    -fx-font-size: 22;
                """);
        error.enqueue(new JFXSnackbar.SnackbarEvent(label, Duration.seconds(5), null));
        barcodeViewer.setImage(null);
        print.setDisable(true);
    }

    /**
     * Places the Image currently shown in the preview in the center of the ImageView.
     */
    private void centerImage() {
        Image img = barcodeViewer.getImage();
        if (img != null) {
            double width;
            double height;
            double ratioX = barcodeViewer.getFitWidth() / img.getWidth();
            double ratioY = barcodeViewer.getFitHeight() / img.getHeight();

            double reducCoeff = Math.min(ratioX, ratioY);

            width = img.getWidth() * reducCoeff;
            height = img.getHeight() * reducCoeff;

            barcodeViewer.setX((barcodeViewer.getFitWidth() - width) / 2);
            barcodeViewer.setY((barcodeViewer.getFitHeight() - height) / 2);

        }
    }

    /**
     * Checks if the code exists within the current database, i.e. is valid.
     * @param code The code to check for validity.
     * @return true if <@code>code</@code> is valid, false if not.
     * @throws IOException if the BufferedReader experiences an I/O Error.
     */
    private boolean checkValid(String code) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Encoder.class.getResourceAsStream("codes.txt"))));
        while (bufferedReader.ready()) {
            String s = bufferedReader.readLine();
            if (s.equals(code)) {
                bufferedReader.close();
                return true;
            }
        }
        return false;
    }

    public void setStage(Stage stage){
        this.stage = stage;
    }

    @FXML
    void closeProgram() { stage.close(); }

    /**
     * Sends a PrintJob to the printer set as the default printer. The printed document will be the barcode with the text it represents
     * written underneath. A black box is drawn around the whole thing in case it needs to be manually cut into the correct size, as a
     * printer's own cutter (if present) can be off by a few millimeters.
     */
    @FXML
    public void adminPrint() {
        BufferedImage barcode = SwingFXUtils.fromFXImage(barcodeViewer.getImage(), null);
        Graphics2D g = barcode.createGraphics();
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, barcode.getWidth()-1, barcode.getHeight()-4);
        g.dispose();
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat format = job.defaultPage();
        format.setOrientation(PageFormat.PORTRAIT);
        Paper paper = format.getPaper();
        paper.setSize(paper.getWidth(), barcode.getHeight());
        paper.setImageableArea(Encoder.fromCMtoPPI(0.3), 0, 288, barcode.getHeight());
        format.setPaper(paper);
        Book book = new Book();
        book.append(new LabelPrinter(barcode), format);
        job.setPageable(book);
        try {
            job.print();
        } catch (PrinterException ignored) {
        }
    }

    /**
     * Custom implementation of Printable prints a BufferedImage.
     * There will be no Print Dialog and the default printer will be used.
     */
    public static class LabelPrinter implements Printable {

        private final BufferedImage image;

        public LabelPrinter(BufferedImage image) {
            this.image = image;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
            int result = NO_SUCH_PAGE;
            if (pageIndex == 0) {
                result = PAGE_EXISTS;
                graphics.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
                graphics.drawImage(image, 0, 0, null);
            }
            return result;
        }
    }
}
