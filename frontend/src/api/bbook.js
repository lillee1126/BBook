import request from '../utils/request'

export const register = (payload) => request.post('/api/v1/auth/register', payload)
export const login = (payload) => request.post('/api/v1/auth/login', payload)
export const fetchMe = () => request.get('/api/v1/auth/me')

export const fetchTags = () => request.get('/api/v1/tags')
export const fetchPosts = (params) => request.get('/api/v1/posts', { params })
export const fetchPostDetail = (id) => request.get(`/api/v1/posts/${id}`)
export const createPost = (payload) => request.post('/api/v1/posts', payload)
export const likePost = (id) => request.post(`/api/v1/posts/${id}/likes`)
export const deletePost = (id) => request.delete(`/api/v1/posts/${id}`)
export const postComment = (id, payload) => request.post(`/api/v1/posts/${id}/comments`, payload)
export const likeComment = (postId, commentId) => request.post(`/api/v1/posts/${postId}/comments/${commentId}/likes`)
export const deleteComment = (postId, commentId) => request.delete(`/api/v1/posts/${postId}/comments/${commentId}`)
export const fetchUserHome = (id) => request.get(`/api/v1/users/${id}`)
export const updateMyProfile = (payload) => request.put('/api/v1/users/me', payload)
export const uploadLocalFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/v1/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
