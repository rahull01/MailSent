import type { ReactNode } from "react";
import "./globals.css";

export const metadata = {
  title: "MailSent",
  description: "AI Email Infrastructure Intelligence Platform",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <body className="min-h-screen bg-slate-50 text-slate-900 antialiased">
        {children}
      </body>
    </html>
  );
}