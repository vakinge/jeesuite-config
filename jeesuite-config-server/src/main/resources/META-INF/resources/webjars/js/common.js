var $ =jQuery =layui.jquery;
layui.define(['layer','laytpl','form'], function(exports) {
	"use strict";
	var $ = layui.jquery,
		layer = layui.layer,
		laytpl = layui.laytpl,
		form = layui.form();
	var common = {
		throwError: function(msg) {
			throw new Error(msg);
			return;
		},
		msgError: function(msg) {
			layer.msg(msg, {
				icon: 5
			});
			return;
		},
		msgSuccess: function(msg) {
			layer.msg(msg, {
				icon: 6
			});
			return;
		},
		setPageBindAttr($container,data){
			$('[bindAttr]',$container).each(function(){
				var self = $(this),bindAttr = self.attr('bindAttr');
				var value;
				if(bindAttr.indexOf('{')==0){
					var attrs = bindAttr.substring(1,bindAttr.length - 1).split(',');
					var jsonObj = {};
					for(var i in attrs){
						jsonObj[attrs[i]] = eval('data.'+attrs[i]);
					}
					self.attr('data',JSON.stringify( jsonObj ));
				}else if(bindAttr.indexOf('template:')==0){
					var templateId = bindAttr.replace(/template:/, '');
					var html = template(templateId, data);
					self.html(html);
				}else{
					try{value = eval('data.'+bindAttr);}catch(e){}
					if(!value)return;
					if(self.is('input')){
						var type = self.attr('type');
						if(type == 'radio'){
							if(self.attr('value') == value+"")self.attr('checked',true);
						}else if(type == 'checkbox'){
							if(self.attr('value') == value+"")self.attr('checked',true);
							var switchElm;
							if(switchElm = self.next('.layui-form-switch')){
								switchElm.addClass('layui-form-onswitch');
							}
						}else{
							self.val(value);
					    }
					}else if(self.is('textArea')){
						self.val(value);
					}else if(self.is('img')){
						self.attr('src',APP.attachRoot + value);
					}else if(self.is('a')){
						self.attr('href',value);
					}else if(self.is('select')){
						self.find("option[value='"+value+"']").attr("selected",true);  
					}else{
						self.html(value);
					}
				}
				self.removeAttr('bindAttr');
			});
		}
	};

	exports('common', common);

	//template
	$('*[templateLoad]').each(function(){
		var $this = $(this),url = $this.attr('templateLoad'),templateId=$this.attr('template-id'),
		method=$this.attr('ajax-method') || 'GET',
		callback = $this.attr('onFinishCallback'),
		templateExtends=$this.attr('template-ext');	
		
		if(!url || url == ''){
			url = getQueryParam("dataloadurl");
		}
		if(!url || url == '')return;
		//if(location.search)url = url + location.search;
		
		var _global = {};//全局拓展只需要加载一次
		var extendsCallback = {};
		if(templateExtends){
			if(templateExtends.indexOf('flowpage') >= 0){}
		}
		//
		renderPage();
		
		function renderPage(params){
			params = params || {};
			if($this.parent().is('form')){
				var query = $this.parent().serializeArray();
				if(query){
					$.each( query, function(i, field){
						params[field.name] = field.value;
					});
				}
			}
			var loading = layer.load();
			var postdata = (params.length && params.length > 0) ? JSON.stringify(params) : (method == 'GET' ? '' : '{}');
			$.ajax({
				dataType:"json",
			    type: method,
		        url: url,
		        contentType: "application/json",
		        data:postdata,
				complete: function(){layer.close(loading);},
				success: function(result){
					if(result.code && result.code==401){top.location.href = "/login.html";return;}
					if(result.code && result.code==403 && result.msg === 'ipForbidden'){top.location.href = "/set_safe_ipaddr.html";return;}
					if(!result.code || result.code == 200){
						var renderData = result.data ? result.data : result;
						if(renderData == null || renderData.length == 0){
							$this.html('暂无数据');
							return;
						}
						if(templateId && templateId != ''){
							var tpl = $('#'+templateId).html();
							laytpl(tpl).render(renderData, function(html){
								$this.html(html);
							});
						}else{
							common.setPageBindAttr($this,renderData);
						}
						//拓展执行
						for(var index in extendsCallback){
							if(!_global[index]){
								extendsCallback[index](renderData);
							}
						}
						//
						if(callback){
						    eval(callback+"(renderData)");
			             }
					}else{
						$this.html("无数据或者加载错误");
					}
				},
				error: function(xhr, type){
					$this.html('系统错误');
				}
			});
		}
	});
	
    //submit
	$('body').on('click','.J_ajaxSubmit',function(){
		var $this = $(this),$form = $this.parent(),callback = $this.attr('onSuccessCallback'),jumpUrl = $this.attr('onSuccessJumpurl');
		while(!$form.is('form')){
			$form = $form.parent();
		}
//		//验证
//		if(!$form.doFormValidator()){
//		  return;
//		}
		var params = {};
		var dataArrays = $form.serializeArray();
		if(dataArrays){
			$.each( dataArrays, function(i, field){
				if(field.value && field.value != ''){					
					if(params[field.name]){
						params[field.name] = params[field.name] + ',' + field.value;
					}else{					
						params[field.name] = field.value;
					}
				}
			});
		}
		$this.attr('disabled',true);
		var loading = layer.load();
		var requestURI = $form.attr('action');
		$.ajax({
			dataType:"json",
		    type: "POST",
	        url: requestURI,
	        contentType: "application/json",
	        data:JSON.stringify(params) ,
			complete: function(){layer.close(loading);},
			success: function(data){
				if(data.code==401){top.location.href = "/login.html";return;}
		        if(data.code==200){
		        	 common.msgSuccess(data.msg || '操作成功');
		             data = data.data;
		             if(callback != undefined){
					    eval(callback+"(data)");
		             }
		             if(jumpUrl){
		            	 setTimeout(function(){window.location.href = jumpUrl;},500);
					 }
		          }else{
		        	 $this.removeAttr('disabled');
		        	 common.msgError(data.msg);
		          }
		        },
			error: function(xhr, type){
				$this.removeAttr('disabled');
				common.msgError('系统错误');
			}
		});
	});
	//
	$('body').on('click','.J_form_dialog', function(){
		var self = $(this),
		     templdateurl = self.attr('templdate-url'),
		     dataurl = self.attr('data-url'),
		     dialogTitle = self.attr('dialog-title') || '表单',
		     onLoadFinished = self.attr('onLoadFinishedCallback');
		var addBoxIndex = -1;
		$.get(templdateurl, null, function(form) {
			addBoxIndex = layer.open({
				type: 1,
				title: dialogTitle,
				content: form,
				btn: ['提交', '取消'],
				shade: false,
				offset: ['100px', '30%'],
				area: ['600px', '400px'],
				zIndex: 99999,
				maxmin: true,
				yes: function(index) {
					//触发表单的提交事件
					$('form.layui-form').find('button[lay-filter=submitbtn]').click();
				},
				full: function(elem) {
					var win = window.top === window.self ? window : parent.window;
					$(win).on('resize', function() {
						var $this = $(this);
						elem.width($this.width()).height($this.height()).css({
							top: 0,
							left: 0
						});
						elem.children('div.layui-layer-content').height($this.height() - 95);
					});
				},
				success: function(layero, index) {
					//弹出窗口成功后渲染表单
					var form = layui.form();
					form.render();
					if(onLoadFinished){
						eval(onLoadFinished+"(form)");
					}
					var $form = $('form.layui-form');
					//
					if(dataurl && dataurl != ""){
						var loading = layer.load();
						$.getJSON(dataurl,function(result){
							layer.close(loading);
							common.setPageBindAttr($form,result.data);
						});
					}
					form.on('submit(submitbtn)', function(data) {
						var loading = layer.load();
						$.ajax({
							dataType:"json",
						    type: "POST",
						    contentType: "application/json",
					        url: $form.attr('action'),
					        data:JSON.stringify(data.field),
							complete: function(){layer.close(loading);},
							success: function(data){
								if(data.code==401){top.location.href = "/login.html";return;}
						        if(data.code == 200){
						        	common.msgSuccess(data.msg || "操作成功");
						        	setTimeout(function(){layer.close(index);},500);
						        	 window.location.reload() ;
						        }else{
						        	common.msgError(data.msg || "操作失败");
						        }
						   },
							error: function(xhr, type){
								$this.removeAttr('disabled');
								common.msgError('系统错误');
							}
						});
						return false; 									
					});
				},
				end: function() {
					addBoxIndex = -1;
				}
			});
		});
	});
	//
	$('body').on('click','.J_confirmurl', function(){
		var self = $(this),
			uri = self.attr('act-url'),
			msg = self.attr('act-msg') || '您确认该操作吗',
			callback = self.attr('act-callback');
		
		layer.confirm(msg, {
		    btn: ['确定','取消'], //按钮

		    shade: false //不显示遮罩

		}, function(index){
	        $.getJSON(uri, function(result){
	        	layer.close(index); 
	        	if(result.code==401){top.location.href = "/login.html";return;}
				if(result.code == 200){
					common.msgSuccess(result.msg);
					setTimeout(function(){
						if(callback != undefined){
							eval(callback);
						}else{
							window.location.reload();
						}
					},500);
				}else{
					common.msgError(result.msg);
				}
			});
	        return false;
	    }, function(index){
	    	layer.close(index); 
		});
	});
	
	$('select[asnycSelect]').each(function(){
		var $this = $(this),url=$this.attr("asnycSelect"),onLoadFinished = $this.attr('onDataLoadCallback'); 
		$.getJSON(url,function(result){
			if(onLoadFinished){
				eval(onLoadFinished+"(result)");
			}
			var opthtml;
			for(var index in result){
				var selected = result[index].selected ? 'selected="selected"' : '';
				opthtml = '<option value="'+result[index].value+'" '+selected+'>'+result[index].text+'</option>';
				$this.append(opthtml);
			}
			form.render('select');
		});
	});
});


function getQueryParam(name){
     var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
     var r = window.location.search.substr(1).match(reg);
     if(r!=null)return  unescape(r[2]); return null;
}

function redirct(url){
	url = url || location.href;
	url = url.replace("?__rnd=","__rnd=").replace("&__rnd=","__rnd=").split("__rnd=")[0];
	var rnd = (url.indexOf("?")>0 ? "&" : "?") + "__rnd=" + new Date().getTime();
	window.location.href = url + rnd;
}

function setCookie(name,value,expireDays){ 
	expireDays = expireDays || 1; 
    var exp = new Date(); 
    exp.setTime(exp.getTime() + expireDays*24*60*60*1000); 
    document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString(); 
} 

//读取cookies 
function getCookie(name) { 
    var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");
    if(arr=document.cookie.match(reg))
        return unescape(arr[2]); 
    else 
        return null; 
} 