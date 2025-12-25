package com.noteplace.readingsummary.report.service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.noteplace.readingsummary.book.repository.BookRepository;
import com.noteplace.readingsummary.session.repository.ReadingSessionRepository;
import com.noteplace.readingsummary.summary.repository.SummaryRepository;

@Service
public class PdfReportService {

  private final BookRepository bookRepo;
  private final SummaryRepository summaryRepo;
  private final ReadingSessionRepository sessionRepo;

  public PdfReportService(BookRepository bookRepo, SummaryRepository summaryRepo, ReadingSessionRepository sessionRepo) {
    this.bookRepo = bookRepo;
    this.summaryRepo = summaryRepo;
    this.sessionRepo = sessionRepo;
  }

  public byte[] generate(Long userId, Long bookId) {
    var book = bookRepo.findByIdAndUserId(bookId, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "book not found"));

    var summaries = summaryRepo.findByBookIdOrderByScopeAscChapterAscIdAsc(bookId);
    var sessions = sessionRepo.findByBookIdOrderBySessionDateDescCreatedAtDesc(bookId);

    try (var out = new ByteArrayOutputStream()) {
      Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
      PdfWriter.getInstance(doc, out);
      doc.open();

      Font titleFont = new Font(loadJapaneseBaseFont(), 18, Font.BOLD);
      Font hFont = new Font(loadJapaneseBaseFont(), 13, Font.BOLD);
      Font bodyFont = new Font(loadJapaneseBaseFont(), 10);

      // Title
      doc.add(new Paragraph("Reading Report", titleFont));
      doc.add(Chunk.NEWLINE);

      // Book meta
      doc.add(new Paragraph("Book", hFont));
      doc.add(new Paragraph("Title: " + book.getTitle(), bodyFont));
      doc.add(new Paragraph("Author: " + nvl(book.getAuthor()), bodyFont));
      doc.add(new Paragraph("Status: " + nvl(book.getStatus()), bodyFont));
      doc.add(new Paragraph("Pages: " + nvl(book.getCurrentPage()) + " / " + nvl(book.getTotalPages()), bodyFont));
      doc.add(Chunk.NEWLINE);

      // Book summary (scope=BOOK)
      doc.add(new Paragraph("Summary (BOOK)", hFont));
      var bookSummary = summaries.stream()
          .filter(s -> "BOOK".equals(s.getScope()))
          .findFirst()
          .map(s -> s.getContentMd())
          .orElse("(no summary)");
      doc.add(new Paragraph(bookSummary, bodyFont));
      doc.add(Chunk.NEWLINE);

      // Chapter summaries
      doc.add(new Paragraph("Summaries (CHAPTER)", hFont));
      var chapterOnes = summaries.stream().filter(s -> "CHAPTER".equals(s.getScope())).toList();
      if (chapterOnes.isEmpty()) {
        doc.add(new Paragraph("(no chapter summaries)", bodyFont));
      } else {
        for (var s : chapterOnes) {
          doc.add(new Paragraph("Chapter " + s.getChapter(), new Font(loadJapaneseBaseFont(), 11, Font.BOLD)));
          doc.add(new Paragraph(s.getContentMd(), bodyFont));
          doc.add(Chunk.NEWLINE);
        }
      }

      // Sessions table
      doc.newPage();
      doc.add(new Paragraph("Reading Sessions", hFont));
      doc.add(Chunk.NEWLINE);

      PdfPTable table = new PdfPTable(new float[]{2, 1, 1, 4});
      table.setWidthPercentage(100);

      addHeader(table, "Date", bodyFont);
      addHeader(table, "Min", bodyFont);
      addHeader(table, "Pages", bodyFont);
      addHeader(table, "Memo", bodyFont);

      var df = DateTimeFormatter.ISO_LOCAL_DATE;
      for (var s : sessions) {
        table.addCell(cell(s.getSessionDate().format(df), bodyFont));
        table.addCell(cell(String.valueOf(s.getMinutes()), bodyFont));
        table.addCell(cell(s.getPagesRead() == null ? "" : String.valueOf(s.getPagesRead()), bodyFont));
        table.addCell(cell(nvl(s.getMemo()), bodyFont));
      }

      doc.add(table);

      doc.close();
      return out.toByteArray();
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "pdf generation failed");
    }
  }

  private BaseFont loadJapaneseBaseFont() throws Exception {
    // フォント埋め込み（日本語対応）
    var res = new ClassPathResource("fonts/NotoSansJP-Regular.ttf");
    byte[] bytes = res.getInputStream().readAllBytes();
    return BaseFont.createFont(
        "NotoSansJP-Regular.ttf",
        BaseFont.IDENTITY_H,
        BaseFont.EMBEDDED,
        true,
        bytes,
        null
    );
  }

  private static void addHeader(PdfPTable table, String text, Font font) {
    PdfPCell c = new PdfPCell(new Phrase(text, font));
    c.setBackgroundColor(new java.awt.Color(230, 230, 230));
    c.setPadding(6);
    table.addCell(c);
  }

  private static PdfPCell cell(String text, Font font) {
    PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, font));
    c.setPadding(6);
    c.setVerticalAlignment(Element.ALIGN_TOP);
    return c;
  }

  private static String nvl(Object o) { return o == null ? "-" : String.valueOf(o); }
}
