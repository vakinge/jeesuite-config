layui.config({
  version: '20190612',
  base : '/assets/js/'
});
layui.define(['jeesuitelayui', 'table','form'], function(exports){

  var $ = layui.jquery
  ,laypage = layui.laypage 
  ,layer = layui.layer 
  ,table = layui.table 
  ,form = layui.form
  ,element = layui.element
  jeesuitelayui = layui.jeesuitelayui;
  
  if(viewType === 'list'){
	  var $table =  table.render({
		    elem: '#tablecont'
		    ,height: 450
		    ,width: 1150
		    ,url: '/admin/app/list'
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
		      {field: 'id', title: 'ID', width:40, fixed: 'left'}
		      ,{field: 'appName', title: '应用名称', width:120}
		      ,{field: 'appKey', title: '应用标识', width:100}
		      ,{field: 'serviceId', title: '服务名称', width:120}
		      ,{field: 'appTypeAlias', title: '类型', width:80}
		      ,{field: 'enabled', title: '状态', width:80, templet: '#enabledTpl'}
		      ,{field: 'master', title: '负责人', width:80}
		      ,{fixed: 'right', title: '操作',width:230, align:'left', toolbar: '#toolBar'}
		    ]],
		  });
	  
	//监听工具条
	  table.on('tool(table)', function(obj){ 
		  var data = obj.data,layEvent = obj.event; 
		  if(layEvent === 'toggle'){
		      layer.confirm('确认禁用么?', function(index){
			    	jeesuitelayui.post('/admin/app/toggle',{id:data.id},function(data){
				        layer.close(index);
				        $table.reload({
							  where: jeesuitelayui.serializeJson($('#searchform'))
							  ,page: {
							    curr: 1
							  }
							});
			    	});
			       
			      });
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
		  //
		  $('body').on('mouseover','.status-up',function(){
			  var $this = $(this),content = $this.next().html();
			  layer.tips(content, $this);
		  });
  }
});