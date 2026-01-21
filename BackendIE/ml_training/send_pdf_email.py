#!/usr/bin/env python3
"""
Script ejemplo para enviar un PDF por SMTP (Python). No modifica nada del backend Java.
Usa variables de entorno para credenciales (EMAIL_USERNAME, EMAIL_PASSWORD, EMAIL_FROM).

Uso:
  python ml_training/send_pdf_email.py --pdf path/to/file.pdf --to dest@example.com
"""
import argparse
import smtplib
import os
from email.message import EmailMessage


def send_pdf(pdf_path, to_email, subject='Documento de compliance', body='Adjunto PDF'):
    user = os.environ.get('EMAIL_USERNAME')
    pwd = os.environ.get('EMAIL_PASSWORD')
    sender = os.environ.get('EMAIL_FROM', user)
    if not user or not pwd:
        raise RuntimeError('Set EMAIL_USERNAME and EMAIL_PASSWORD env vars')

    msg = EmailMessage()
    msg['Subject'] = subject
    msg['From'] = sender
    msg['To'] = to_email
    msg.set_content(body)

    with open(pdf_path, 'rb') as f:
        data = f.read()
    msg.add_attachment(data, maintype='application', subtype='pdf', filename=os.path.basename(pdf_path))

    # Example using Gmail SMTP (cambiar host/puerto según tu proveedor)
    smtp_host = os.environ.get('SMTP_HOST', 'smtp.gmail.com')
    smtp_port = int(os.environ.get('SMTP_PORT', '587'))

    with smtplib.SMTP(smtp_host, smtp_port) as s:
        s.starttls()
        s.login(user, pwd)
        s.send_message(msg)


if __name__ == '__main__':
    p = argparse.ArgumentParser()
    p.add_argument('--pdf', required=True)
    p.add_argument('--to', required=True)
    p.add_argument('--subject', default='Documento de compliance')
    args = p.parse_args()
    send_pdf(args.pdf, args.to, subject=args.subject)
    print('Sent', args.pdf, 'to', args.to)

