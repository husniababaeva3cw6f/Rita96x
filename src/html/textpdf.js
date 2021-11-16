/* TextPDF - generate PDF dynamically
 * 
 * Copyright (c) 2015 Lucky Byte, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
var focus_input;


function onSaveButtonClick()
{
}

function onCheckButtonClick()
{
  var inputs = $(':input[type=text]');
  var total = inputs.length;
  var empty = 0;

  inputs.each(function(index, elem) {
    if ($(this).val() == "") {
      empty++;
    }
  });

  if (empty > 0) {
    var msg = "Total: " + total + "\n";
    msg += "Remains: " + empty + "\n";
    alert(msg);
  }
}

function onPrevButtonClick()
{
  if (!focus_input) {
    $(':input[type=text]').first().focus();
    return;
  }
  var inputs = $(':input[type=text]');
  var index = inputs.index(focus_input);
  if (index > 0) {
    inputs.eq(index - 1).focus();
  } else {
    focus_input.focus();
  }
}

function onNextButtonClick()
{
  if (!focus_input) {
    $(':input[type=text]').first().focus();
    return;
  }
  var inputs = $(':input[type=text]');
  var index = inputs.index(focus_input);
  if (inputs.length > index + 1) {
    inputs.eq(index + 1).focus();
  } else {
    focus_input.focus();
  }
}


function onCloseButtonClick()
{
  window.close();
}


$(function() {
  $('#save-button').click(onSaveButtonClick);
  $('#check-button').click(onCheckButtonClick);
  $('#prev-button').click(onPrevButtonClick);
  $('#next-button').click(onNextButtonClick);
  $('#close-button').click(onCloseButtonClick);

  $('input[type=text]').focus(function() {
    focus_input = $(this);
  });
});

