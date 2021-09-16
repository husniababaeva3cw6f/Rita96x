# 转换文本文件为PDF

给定一个 XML 模板文件，然后通过 Json 提供模板数据，整合后生成 PDF 文档。

这个项目可以用于需要动态生成 PDF 的场景，例如试卷、合同，等等。

## XML 模板

模板提供文档的固定内容，然后留出数据混入的标记，下面是一个简单的例子：

```xml
<textpdf>
  <para>这是固定内容，<value id="json_key" />继续固定内容。</para>
</textpdf>
```

上面`<value id="json_key" />`部分会从 JSON 数据源中通过 `json_key` 获取数据填入其中，其它部分为固定内容。

TextPDF 的 XML 模板侧重于描述排版效果，例如'字体大小'，'粗体'，'斜体'，'段落缩进'，'段前段后空间'，等等。以便能生成满意的 PDF 文档。模板中唯一动态的内容是`<value id="some">`标签，其会被 JSON 中的同名 key 值替换掉，下面是一个更加丰富的模板示例:

```xml
<!-- 模板根标签必须是 textpdf -->
<textpdf>
    <title>这是一段标题，它的默认格式和普通段落不一样，字体要大一些，并且居中显示</title>
    <title font-size="12">可以直接指定标题的字体大小来改变默认值</title>
    <section>这是一个比标题略小，左对齐的段落</section>
    <para>普通段落</para>
    <para font-family="heiti" font-size="11" font-style="bold,underline,italic" align="right" indent="22" space-before="12" space-after="20">这个段落定义了许多格式</para>
    <para>可以通过<span font-style="bold">span元素来嵌套文字风格，</span>这样可以在一个段落中出现多种风格。</para>
</textpdf>
```
TextPDF 的模板只支持简单的排版格式（[查看模板说明](http://git.oschina.net/lucky-byte/textpdf/wikis/Template)）。

### 转换 .doc 文件

TextPDF 可以将`.doc`文件转换成 TextPDF 的 XML 模板文件，对于`.doc`中的`___________`会自动转换为 XML 模板的 `<value>`标签，这样后续再通过整合 JSON 数据来合成最终的 PDF 文件。

## JSON 数据

模板的数据源以 JSON 格式提供，格式非常简单，所有模板数据放在`data`对象中，其它不限，例如：

```json
{
    "data" : {
        "key1": "value1",
        "key2": "value2"
    },

    "your" : "Some",
    "meta" : "Other"
}
```

### JSON 数据源

通常的情况是用户根据 XML 模板来录入那些需要填充的字段，并保存到数据库或文件中，后续再通过程序来合成 PDF。

## HTML 编辑

在实际应用中，用户需要从某个地方输入 XML 模板中的录入域(`<value>`)，为此，TextPDF 可以将 XML 模板转换为 HTML 文件，所有的`<value>`标签会转换为 HTML 的输入框，用户只能录入这些输入框的数据。

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