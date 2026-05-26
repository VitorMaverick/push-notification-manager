package br.edu.acad.ifma.device.usecase;

public record RegisterDeviceCommand(String fcmToken, String platform, String userAgent) {}
