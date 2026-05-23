import axios from "axios"
 
const httpInstance=axios.create({
  baseURL: '/api/v1',
  timeout:10000
})

httpInstance.interceptors.request.use(
  config =>{
    return config
},
error => Promise.reject(error)
)

httpInstance.interceptors.response.use(
  res =>{
    return res.data
  },
  error => Promise.reject(error)
)
 
  export default httpInstance 