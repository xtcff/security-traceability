var vm = new Vue({
	el: '#login',
	data() {
		return {
			ruleForm: {
				username: '',
				password: ''
			},
			rules: {
				username: {
					required: true,
					message: '请输入用户名',
					trigger: 'blur'
				},
				password: {
					required: true,
					message: '请输入密码',
					trigger: 'blur'
				},
			},

		}
	},
	methods: {
		submitForm() {
			this.$refs['ruleForm'].validate((valid) => {
				if (valid) {
					 axios.post("/login/admin", this.ruleForm).then((res) => {
					 	console.log(res.data)
					 	if (res.status === 200) {
					 		window.location.href = "../../index.html"
					 		sessionStorage.setItem("username",res.data.username)
					 	}
					 	layer.msg("登录成功！")
					 }).catch(function (error){
					 	console.log(error)
						layer.msg("用户名或密码错误！")
					 })
				} else {
					console.log('error submit!!');
					return false;
				}
			});
		},
	}
})
