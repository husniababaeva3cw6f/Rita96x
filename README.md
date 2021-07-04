# 转换文本文件为PDF

给定一个 XML 模板文件，然后通过 Json 提供模板数据，整合后生成 PDF 文档。

这个项目可以用于需要动态生成 PDF 的场景，例如试卷、合同，等等。

## XML 模板

模板提供文档的固定内容，然后留出数据混入的标记，下面是一个简单的例子：

```xml
<p>这是固定内容，
<value name="json_key" />
继续固定内容。
</p>
```

上面`<value name="json_key" />`部分会从 JSON 数据源中通过 `json_key` 获取数据填入其中，其它部分为固定内容。

TextPDF 的模板是非常简单的，不能满足复杂的排版要求。

## JSON 数据

模板的数据源以 JSON 格式提供，这是一个简单的 Hash 对，没有嵌套，例如：

```json
{
    key1: value1,
    key2: value2,
}
```

## 用法

### 命令行用法

```sh
java -jar textpdf xmlfile jsonfile
```

其中 `xmlfile` 和 `jsonfile` 分别是模板和数据源，生成的 PDF 文件和 `xmlfile` 同名，后缀为 `.pdf`。

### 程序调用

```java

import com.lucky_byte.pdf.TextPDF;

try {
    File xmlfile = new File("path/to/xmlfile");
    File jsonfile = new File("path/to/jsonfile");
    File pdffile = new File("path/to/pdffile");
    TextPDF.gen(xmlfile, jsonfile, file.pdf);
} catch (Exception ex) {
    ex.printStackTrace();
}
```
