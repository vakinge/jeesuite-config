/**
 * 无限级联异步加载下拉框插件
 * Copyright (c) 2013 vakinge@gmail.com
 * */
;(function($) {
	// 异步加载下拉框
	$.fn.asnycSelect = function(options) {
		return this.each(function(){
			  new asnycSelect().builder($(this),options);
		});
	};
	
	$.fn.setAsnycSelectVal = function(vals) {
		if(!vals)return;
		vals = vals.split('-');
		var $select,i = 0,interval = null;
		$select = this.eq(0);
	    interval=setInterval(function(){
	       if($select.children().length > 1){
	    	   if(!vals[i] || vals[i]==''){clearInterval(interval);return;}
	    	   $select.find("option[value='"+vals[i]+"']").attr("selected",true).change();
	         i++;
	         $select = $select.next();
	         if(i == vals.length){
	            clearInterval(interval);
	         }
	       }
	    }, 500);
	};
	function asnycSelect(){
		this.settings = {
			linkage:false,//是否联动菜单
			actionUrl:null,//请求地址
			valtarget:null,//值输出元素（选择的值最终保存在）
			pid:0,//
			level: 1,
			maxlevel:1//最大级 默认：1
		};
		this.builder = function($target,options){
	    	options = $.extend(this.settings,options);
			var $subSelect,settings = this.settings;
			$target.attr("level",settings.level).empty();
			$.ajax({
				  type: "GET",
				  url: settings.actionUrl.replace(/_PID_/, settings.pid),
				  cache: true,
				  dataType: "json",
				  success: function(result){
				   if(!result)return;
				   $target.append("<option value=''>请选择</option>");
				   for(var i in result){
					   $target.append("<option value='"+i+"'>"+result[i]+"</option>");
				   }
				   var linkages = $target.parent().find('select').length;
				   var linkNext = settings.linkage && settings.maxlevel > linkages &&settings.maxlevel > settings.level;
				   if(linkNext){
					   $subSelect = $target.clone();
					   $subSelect.empty().append("<option value=''>请选择</option>");
					   $subSelect.insertAfter($target);
				   
				   }
				   $target.on('change',function(){
					   var $this = $(this),
					       selectVal='',selectText='';//
	                   //多级下拉框值用“-”连接,如：123-234
					   if($this.val() == '')return;
					   $this.parent().find('select').each(function(index){
						   var val = $(this).val();
						   if(val !=null && val != '' && val != 'null'){
							   selectVal = selectVal + (index == 0 ? "" : "-") + val;
							   selectText =selectText + (index == 0 ? "" : "-") + $(this).find("option:selected").text();
						   }
						   //仅读取当前下拉框的值
						   if($this.attr("level") == $(this).attr("level")){return false;}
						   
					   });
					   $(settings.valtarget).val(selectVal).attr("text",selectText);
					   if(!linkNext)return;
					   options.level = parseInt($this.attr("level")) + 1;
					   options.pid = $this.val();
					   $subSelect.asnycSelect(options);
				   });
				}
				});
	  };
	};		  
})(jQuery);



