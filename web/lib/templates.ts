export const TEMPLATE_STORAGE_KEY = "aipyq_template_instruction";
export const TEMPLATE_EVENT = "aipyq-template-selected";

export function emitTemplateSelection(instruction: string) {
  if (typeof window === "undefined") return;
  window.localStorage.setItem(TEMPLATE_STORAGE_KEY, instruction);
  window.dispatchEvent(new Event(TEMPLATE_EVENT));
}
