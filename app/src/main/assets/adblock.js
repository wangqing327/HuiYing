function runjs() {
    //console.log('广告过滤数组:'+a)
    for (var j = 0; j < a.length; j++) {
        var els = findElement(a[j])
        //console.log('广告元素长度:'+els.length)
        for (var i = 0; i < els.length; i++) {
            //console.log(els[i])
            const element = els[i];
            element.style.cssText = "display:none"
            // 删除自身
            element.parentNode.removeChild(element)
        }
    }
}
/**
 *
 * @param {*} element
 * @returns 返回css
 */
function getStyle(element) {
    return document.defaultView.getComputedStyle(element)
}

/**
 * 寻找元素
 * @param {*} tag 元素标识 #id .class div img
 * @param attr    元素属性 src href style等
 * @returns 返回寻找到的元素集合
 */
function findElement(tag) {
    var element = document.querySelectorAll(tag)
    return element
}
//监控浏览器元素改变事件
const body = document.body
const config = {attributes:true,childList:true,subtree:true}
const callback = function(mutationsList,observer){
   // console.log('元素改变')
	runjs()
}
const observer = new MutationObserver(callback)
observer.observe(body,config)