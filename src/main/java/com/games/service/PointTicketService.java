package com.games.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.games.exception.ResourceNotFoundException;
import com.games.model.PointsDetails;
import com.games.payload.PointPlayResponse;
import com.games.payload.Points;
import com.games.repository.PointPlayRepository;
import com.games.repository.SequenceRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.BarcodeImageHandler;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Service
public class PointTicketService {

    @Autowired
    private PointPlayRepository gamePlayRepository;

    @Autowired
    private SequenceRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final java.awt.Font BARCODE_TEXT_FONT = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 14);

    public ByteArrayInputStream citiesReport(String ticketId) {
        PointsDetails pointsDetails = gamePlayRepository.findByTicketId(ticketId);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        try {
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{1, 2, 2});
            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
            PdfPCell hcell = new PdfPCell(new Phrase("Sr No", headFont));
            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(hcell);
            hcell = new PdfPCell(new Phrase("Number", headFont));
            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(hcell);
            hcell = new PdfPCell(new Phrase("Quantity", headFont));
            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(hcell);
            ArrayList<Points> points = objectMapper.readValue(pointsDetails.getPoints(), new TypeReference<ArrayList<Points>>(){});
            System.out.println(points);
            PdfPCell cell;
            int i = 1;
            for (Points pt : points) {
                for (Integer point : pt.getPoints().keySet()) {
                    cell = new PdfPCell(new Phrase(""+i++));
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);

                    cell = new PdfPCell(new Phrase(""+point));
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setHorizontalAlignment(Element.ALIGN_MIDDLE);
                    table.addCell(cell);

                    cell = new PdfPCell(new Phrase(""+pt.getPoints().get(point)));
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setHorizontalAlignment(Element.ALIGN_MIDDLE);
                    table.addCell(cell);
                }

            }
            PdfWriter.getInstance(document, out);
            document.setPageSize(PageSize.A7);
            document.setMargins(2,2,2,2);
            document.open();
            Paragraph title = new Paragraph("** FOR AMUSEMENT ONLY **\n",headFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            Paragraph ticketTime = new Paragraph("Ticket Time: "+pointsDetails.getCreationTime());
            document.add(ticketTime);
            Paragraph drawTime = new Paragraph("Draw Time: "+pointsDetails.getDrawTime().substring(0,2)+":"+pointsDetails.getDrawTime().substring(2,4));
            document.add(drawTime);
            Paragraph deskId = new Paragraph("Desk Id: "+pointsDetails.getRetailId());
            document.add(deskId);
            Paragraph tktId = new Paragraph("Ticket Id: "+pointsDetails.getTicketId());
            document.add(tktId);
            document.add(new Chunk());
            document.add(table);
            document.add(generateEAN13BarcodeImage(pointsDetails.getTicketId()));
            document.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    public Image generateEAN13BarcodeImage(String barcodeText) throws Exception {
        Barcode barcode = BarcodeFactory.createEAN13(barcodeText);
        barcode.setFont(BARCODE_TEXT_FONT);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(BarcodeImageHandler.getImage(barcode), "png", baos);
        return Image.getInstance(baos.toByteArray());
    }

    public static void main(String[] args) throws Exception {
        ImageIO.write(generateCode128BarcodeImage("123456789w"), "png", new File("img.png"));
    }

    public static BufferedImage generateCode128BarcodeImage(String barcodeText) {
        Code128Bean barcodeGenerator = new Code128Bean();
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(160, BufferedImage.TYPE_BYTE_BINARY, false, 0);

        barcodeGenerator.generateBarcode(canvas, barcodeText);
        return canvas.getBufferedImage();
    }

    public static BufferedImage generateQRCodeImage(String barcodeText) throws Exception {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix =
                barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 200, 200);

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }


    public Image generateQRBarcodeImage(String barcodeText) throws Exception {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE,3,3);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(MatrixToImageWriter.toBufferedImage(bitMatrix), "jpg", baos);
        return Image.getInstance(baos.toByteArray());
    }
    /*public Image generateEAN13BarcodeImage(String barcodeText, PdfWriter pdfWriter) throws Exception {
        BarcodeEAN codeEAN = new BarcodeEAN();
        codeEAN.setCode(barcodeText.trim().substring(0,10));
        codeEAN.setCodeType(BarcodeEAN.EAN13);
        Image codeEANImage = codeEAN.createImageWithBarcode(pdfWriter.getDirectContent(), null, null);
        return codeEANImage;
    }*/
}
