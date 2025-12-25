package com.noteplace.readingsummary.report.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.noteplace.readingsummary.auth.security.AuthUser;
import com.noteplace.readingsummary.report.service.PdfReportService;

@RestController
@RequestMapping("/api/books")
public class ReportController {

  private final PdfReportService pdfReportService;

  public ReportController(PdfReportService pdfReportService) {
    this.pdfReportService = pdfReportService;
  }

  @GetMapping(value = "/{bookId}/report.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> report(@AuthenticationPrincipal AuthUser user, @PathVariable Long bookId) {
    byte[] pdf = pdfReportService.generate(user.id(), bookId);

    // ファイル名（最低限）
    String filename = "reading-report-" + bookId + ".pdf";
    String cd = "attachment; filename=\"" + filename + "\"";

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, cd)
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdf);
  }
}
