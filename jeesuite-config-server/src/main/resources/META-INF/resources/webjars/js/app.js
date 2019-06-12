layui.config({
  version: '20190612',
  base : '/webjars/js/'
});
layui.define(['jeesuitelayui', 'table','form'], function(exports){

  var $ = layui.jquery
  ,laypage = layui.laypage 
  ,layer = layui.layer 
  ,table = layui.table 
  ,form = layui.form
  ,element = layui.element
  jeesuitelayui = layui.jeesuitelayui;
  
  if(moduleName === 'config-list'){
	  var $table =  table.render({
		    elem: '#tablecont'
		    ,height: 430
		    ,width: 1150
		    ,url: '/admin/config/list'
		    ,method: 'POST'
		   // ,contentType: 'application/json'
		    ,page: true 
		    ,request: {
		         pageName: 'pageNo' 
		    	 ,limitName: 'pageSize' 
		    } 
		    ,response: {
		    	  statusName: 'code' 
		    	  ,statusCode: 200
		    	  ,dataName: 'data'
		    	}
		    ,cols: [[ //表头
		      {field: 'id', title: 'ID', width:60, sort: true, fixed: 'left'}
		      ,{field: 'appNames', title: '应用', width:160}
		      ,{field: 'env', title: '环境', width:60}
		      ,{field: 'name', title: '配置名称', width:170}
		      ,{field: 'typeAlias', title: '类型', width:100}
		      ,{field: 'version', title: '版本', width:90}
		      ,{field: 'updatedBy', title: '更新人', width:90}
		      ,{field: 'updatedAt', title: '更新时间', width:160}
		      ,{fixed: 'right', width: 250, align:'center', toolbar: '#toolBar'}
		    ]],
		  });
		  
		  //监听工具条
		  table.on('tool(table)', function(obj){ 
			  var data = obj.data,layEvent = obj.event; 
			  if(layEvent === 'del'){
			      layer.confirm('确认删除么?', function(index){
			    	jeesuitelayui.post('/admin/config/delete',{id:data.id},function(data){
			    		obj.del(); 
				        layer.close(index);
				        jeesuitelayui.success('删除成功');
			    	});
			       
			      });
			    }else if(layEvent === 'viewContent'){
			    	parent.parent.layer.open({
			    	    type: 1 
			    	    ,area: ['1024px', '660px']
			    	    ,title: '配置内容'
			    	    ,shade: 0.3 //遮罩透明度
			    	    ,maxmin: true //允许全屏最小化
			    	    ,anim: 1 //0-6的动画形式，-1不开启
			    	    ,content: '<div style="padding:5px;"><textarea id="pop_fileContent" style="width:1000px;height:550px;border:1px dashed #000;">'+data.contents+'</textarea> </div>'
			    	    ,btn: ['确认']
			    	    ,yes: function(index, layero){
			    	    	var content = $('#pop_fileContent', window.parent.parent.document).val();
			    	    	$('#fileContent').val(content);
			    	    	parent.parent.layer.close(index);
			    	    }
			    	  }); 
			    }else if(layEvent === 'viewVersion'){
			    	jeesuitelayui.iframeDialog('历史版本','./historyList.html?id='+data.id);
			    }else if(layEvent === 'edit'){
			    	jeesuitelayui.iframeDialog('编辑配置','./edit.html?id='+data.id);
			    }
		  });
		  
		  $('.J_search').on('click',function(){
			   $table.reload({
				  where: jeesuitelayui.serializeJson($('#searchform'))
				  ,page: {
				    curr: 1
				  }
				});
		  });
  }
});