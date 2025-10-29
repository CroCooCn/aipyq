package com.aipyq.friendapp.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prompts")
public class PromptProperties {

    private final Copy copy = new Copy();
    private final Image image = new Image();

    public Copy getCopy() {
        return copy;
    }

    public Image getImage() {
        return image;
    }

    public static class Copy {
        /**
         * 主体模板，支持占位符：
         * {{imageUrl}}、{{styleInstruction}}、{{historySection}}
         */
        private String template;
        /**
         * 历史列表标题。
         */
        private String historyHeader;
        /**
         * 有历史时的单项模板，占位符：{{instruction}}、{{outputs}}
         */
        private String historyItem;
        /**
         * 无历史时的提示文案。
         */
        private String historyEmpty;
        /**
         * 默认输出条数提示，可选。
         */
        private List<String> outputRequirements;

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }

        public String getHistoryHeader() {
            return historyHeader;
        }

        public void setHistoryHeader(String historyHeader) {
            this.historyHeader = historyHeader;
        }

        public String getHistoryItem() {
            return historyItem;
        }

        public void setHistoryItem(String historyItem) {
            this.historyItem = historyItem;
        }

        public String getHistoryEmpty() {
            return historyEmpty;
        }

        public void setHistoryEmpty(String historyEmpty) {
            this.historyEmpty = historyEmpty;
        }

        public List<String> getOutputRequirements() {
            return outputRequirements;
        }

        public void setOutputRequirements(List<String> outputRequirements) {
            this.outputRequirements = outputRequirements;
        }
    }

    public static class Image {
        /**
         * 渲染提示词中固定部分，支持占位符：
         * {{ratio}}、{{resolution}}、{{watermarkLine}}
         */
        private String renderTemplate;
        /**
         * 需要水印时的额外指令。
         */
        private String watermarkInstruction;

        public String getRenderTemplate() {
            return renderTemplate;
        }

        public void setRenderTemplate(String renderTemplate) {
            this.renderTemplate = renderTemplate;
        }

        public String getWatermarkInstruction() {
            return watermarkInstruction;
        }

        public void setWatermarkInstruction(String watermarkInstruction) {
            this.watermarkInstruction = watermarkInstruction;
        }
    }
}
