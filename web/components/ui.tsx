import React from "react";

export const palette = {
  background: "#f4f6fb",
  surface: "#ffffff",
  surfaceMuted: "#f2f4f8",
  surfaceElevated: "#e9ecf5",
  primary: "#2563eb",
  primarySoft: "#e1efff",
  accent: "#ff6b6b",
  text: "#0f172a",
  textMuted: "#616d86",
  border: "#d9deeb",
  borderStrong: "#b9c2d8",
  success: "#0ea5e9",
  warning: "#f59e0b"
};

export const radii = {
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  pill: 999
};

export const layout = {
  maxWidth: 1080
};

export type ButtonVariant = "primary" | "secondary" | "ghost" | "danger";

export const buttonStyles: Record<ButtonVariant, React.CSSProperties> = {
  primary: {
    background: palette.primary,
    color: "#fff",
    border: "none"
  },
  secondary: {
    background: palette.surface,
    color: palette.primary,
    border: `1px solid ${palette.border}`
  },
  ghost: {
    background: "transparent",
    color: palette.text,
    border: `1px solid ${palette.border}`
  },
  danger: {
    background: palette.accent,
    color: "#fff",
    border: "none"
  }
};

export function Button({
  variant = "primary",
  style,
  children,
  ...props
}: React.ButtonHTMLAttributes<HTMLButtonElement> & { variant?: ButtonVariant }) {
  return (
    <button
      style={{
        padding: "10px 20px",
        borderRadius: radii.pill,
        fontWeight: 600,
        fontSize: 14,
        cursor: props.disabled ? "not-allowed" : "pointer",
        transition: "transform 120ms ease, box-shadow 120ms ease",
        boxShadow: props.disabled
          ? "none"
          : variant === "primary" || variant === "danger"
            ? "0 10px 20px rgba(37,99,235,0.18)"
            : "none",
        opacity: props.disabled ? 0.6 : 1,
        ...buttonStyles[variant],
        ...style
      }}
      {...props}
    >
      {children}
    </button>
  );
}

export function Card({
  children,
  highlight = false,
  style
}: React.PropsWithChildren<{ highlight?: boolean; style?: React.CSSProperties }>) {
  return (
    <div
      style={{
        background: palette.surface,
        borderRadius: radii.lg,
        border: `1px solid ${highlight ? palette.primarySoft : palette.border}`,
        padding: 24,
        boxShadow: highlight ? "0 24px 40px rgba(37,99,235,0.12)" : "0 12px 32px rgba(15,23,42,0.08)",
        ...style
      }}
    >
      {children}
    </div>
  );
}

export function Section({
  title,
  description,
  action,
  children,
  style
}: {
  title: string;
  description?: string;
  action?: React.ReactNode;
  children: React.ReactNode;
  style?: React.CSSProperties;
}) {
  return (
    <section style={{ display: "grid", gap: 18, ...style }}>
      <header style={{ display: "flex", justifyContent: "space-between", gap: 16, alignItems: description ? "flex-start" : "center" }}>
        <div style={{ display: "grid", gap: 6 }}>
          <h2 style={{ margin: 0, fontSize: 22, color: palette.text }}>{title}</h2>
          {description ? (
            <p style={{ margin: 0, color: palette.textMuted, lineHeight: 1.6, maxWidth: 640 }}>{description}</p>
          ) : null}
        </div>
        {action}
      </header>
      {children}
    </section>
  );
}

export function Badge({
  children,
  tone = "neutral"
}: React.PropsWithChildren<{ tone?: "neutral" | "accent" | "warning" | "success" }>) {
  const tones: Record<string, React.CSSProperties> = {
    neutral: { background: palette.surfaceElevated, color: palette.textMuted },
    accent: { background: "#ffecec", color: palette.accent },
    warning: { background: "#fff6e6", color: palette.warning },
    success: { background: "#e0f7ff", color: palette.success }
  };
  return (
    <span
      style={{
        display: "inline-flex",
        alignItems: "center",
        padding: "4px 10px",
        borderRadius: radii.pill,
        fontSize: 12,
        fontWeight: 600,
        letterSpacing: 0.2,
        ...tones[tone]
      }}
    >
      {children}
    </span>
  );
}

export function Stepper({
  steps,
  current
}: {
  steps: { title: string; description?: string }[];
  current: number;
}) {
  return (
    <ol style={{ listStyle: "none", padding: 0, margin: 0, display: "flex", gap: 20 }}>
      {steps.map((step, index) => {
        const active = index === current;
        const complete = index < current;
        return (
          <li
            key={step.title}
            style={{
              flex: 1,
              display: "grid",
              gap: 6,
              padding: 16,
              borderRadius: radii.md,
              border: `1px solid ${active ? palette.primary : palette.border}`,
              background: active ? palette.primarySoft : complete ? palette.surfaceMuted : palette.surface,
              transition: "all 160ms ease"
            }}
          >
            <span style={{ fontSize: 12, fontWeight: 600, color: active ? palette.primary : palette.textMuted }}>
              {String(index + 1).padStart(2, "0")}
            </span>
            <span style={{ fontWeight: 600, color: palette.text }}>{step.title}</span>
            {step.description ? (
              <span style={{ fontSize: 12, color: palette.textMuted, lineHeight: 1.4 }}>{step.description}</span>
            ) : null}
          </li>
        );
      })}
    </ol>
  );
}

export function InfoRow({
  label,
  value,
  hint
}: {
  label: string;
  value: React.ReactNode;
  hint?: string;
}) {
  return (
    <div style={{ display: "grid", gap: 4 }}>
      <span style={{ fontSize: 13, color: palette.textMuted }}>{label}</span>
      <span style={{ fontSize: 16, fontWeight: 600, color: palette.text }}>{value}</span>
      {hint ? <span style={{ fontSize: 12, color: palette.textMuted }}>{hint}</span> : null}
    </div>
  );
}

export function StatCard({
  label,
  value,
  delta,
  icon
}: {
  label: string;
  value: string;
  delta?: string;
  icon?: React.ReactNode;
}) {
  return (
    <div
      style={{
        background: palette.surface,
        borderRadius: radii.lg,
        border: `1px solid ${palette.border}`,
        padding: 20,
        display: "grid",
        gap: 12
      }}
    >
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <span style={{ fontSize: 13, color: palette.textMuted }}>{label}</span>
        {icon}
      </div>
      <strong style={{ fontSize: 26, color: palette.text }}>{value}</strong>
      {delta ? <span style={{ fontSize: 12, color: palette.success }}>{delta}</span> : null}
    </div>
  );
}

export function Divider() {
  return <div style={{ height: 1, background: palette.border, width: "100%" }} />;
}
