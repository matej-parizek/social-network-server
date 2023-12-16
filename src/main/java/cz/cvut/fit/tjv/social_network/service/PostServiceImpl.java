package cz.cvut.fit.tjv.social_network.service;

import cz.cvut.fit.tjv.social_network.domain.Post;
import cz.cvut.fit.tjv.social_network.domain.PostKey;
import cz.cvut.fit.tjv.social_network.repository.PostRepository;
import cz.cvut.fit.tjv.social_network.repository.UserRepository;
import cz.cvut.fit.tjv.social_network.service.exceptions.post.PostDoesNotExistException;
import cz.cvut.fit.tjv.social_network.service.exceptions.user.UserDoestExistException;
import cz.cvut.fit.tjv.social_network.service.exceptions.user.UsersAreNotFriendsException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@Component
public class PostServiceImpl extends AbstractCrudServiceImpl<Post, PostKey> implements PostService{
    PostRepository postRepository;
    UserRepository userRepository;
//    public PostServiceImpl(PostRepository repository) {
//        this.repository = repository;
//    }

    public PostServiceImpl(PostRepository repository, UserRepository userRepository) {
        this.postRepository = repository;
        this.userRepository = userRepository;
    }

    @Override
    public Post update(Post entity) {
        /// TODO: 20.10.2023
        throw new RuntimeException();
    }

    @Override
    protected JpaRepository<Post, PostKey> getRepository() {
        return postRepository;
    }

    @Override
    public void coCreator(String author, long id, String coAuthor) {
        var optUserAuthor = userRepository.findById(author);
        var optUserCoAuthor = userRepository.findById(coAuthor);
        if(optUserCoAuthor.isEmpty() || optUserAuthor.isEmpty())
        {
            throw new UserDoestExistException();
        }
        var userAuthor = optUserAuthor.get();
        var userCoAuthor = optUserCoAuthor.get();

        // Check friends
        if(!userRepository.findFriends(author).contains(userCoAuthor) || userCoAuthor.equals(userAuthor)) {
            throw new UsersAreNotFriendsException();
        }
        var optPost = postRepository.findById(new PostKey(userAuthor,id));
        if(optPost.isEmpty())
        {
            throw new PostDoesNotExistException();
        }
        var post = optPost.get();
        var newPost = new Post(post.getKey().getId(),userCoAuthor); newPost.setAdded(LocalDateTime.now());
        newPost.setText(post.getText()+ "\n Author: "+author+"\n Co-Author: "+coAuthor+"\n");
        postRepository.save(newPost);
    }

    @Override
    public void like(String who, long uri, String author) {
        var optUserWho = userRepository.findById(who);
        var optUserAuthor = userRepository.findById(author);
        if(optUserWho.isEmpty() || optUserAuthor.isEmpty())
            throw new UserDoestExistException();
        var userWho = optUserWho.get();
        var userAuthor = optUserAuthor.get();
        var optPost = postRepository.findById(new PostKey(userAuthor,uri));
        if(optPost.isEmpty())
            throw new PostDoesNotExistException();
        var post = optPost.get();

        post.getLikes().add(userWho);
        postRepository.save(post);
    }

    @Override
    public Collection<Post> readAllPostByAuthor(String author) {
        var authorOfPosts = userRepository.findById(author);
        if(authorOfPosts.isEmpty())
            throw new UsersAreNotFriendsException();
        return postRepository.findAllByKeyAuthor(authorOfPosts.get());
    }

    @Override
    public Optional<Post> readById(String username, long id) {
        var userOpt=userRepository.findById(username);
        if(userOpt.isEmpty())
            throw new UserDoestExistException();

        var post = postRepository.findById(new PostKey(userOpt.get(),id));
        if(post.isEmpty())
            throw new PostDoesNotExistException();
        return post;
    }
}
