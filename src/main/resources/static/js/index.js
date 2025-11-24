$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");
	var title =$("#recipient-name").val();
	var content =$("#message-text").val();

	$.post(
		CONTEXT_Path+"/discuss/add",
		{"title":title,"content":content},
		function (data){
			data=$.parseJSON(data);
			//提示框输出信息
			$("#hintBody").text(data.msg);
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				if(data.code == 0){
					//成功后刷新界面
					window.location.reload();
				}
			}, 2000);
		}

	);

}